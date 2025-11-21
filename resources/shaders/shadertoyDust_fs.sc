$input v_texcoord0

#include <bgfx_shader.sh>

uniform vec4 iResolution;
uniform vec4 iMouse;
uniform vec4 iTime;

float material, total;

#define ss(a,b,t) smoothstep(a,b,t)

vec3 repeat(vec3 p, float r) 
{ 
    return mod(p, r) - r/2.0; 
}

mat2 rot(float a) 
{ 
    float c = cos(a);
    float s = sin(a);
    return mat2(c, -s, s, c); 
}

float gyroid(vec3 p) 
{ 
    return dot(sin(p), cos(p.yzx)); 
}

const uint k = 1103515245U;
vec3 hash(uvec3 x)
{
    x = ((x>>8U)^x.yzx)*k;
    x = ((x>>8U)^x.yzx)*k;
    x = ((x>>8U)^x.yzx)*k;
    return vec3(x)*(1.0/float(0xffffffffU));
}

float hash12(vec2 p)
{
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float noise(inout vec3 p)
{
    float result = 0.0;
    float a = 0.5;
    for (float i = 0.0; i < 3.0; i += 1.0)
    {
        result += (gyroid(p/a)*a);
        a /= 2.0;
    }
    return result;
}

float noise2(vec3 p)
{
    float result = 0.0;
    float a = 0.5;
    for (float i = 0.0; i < 6.0; i += 1.0)
    {
        p.z += result * 0.5;
        result += abs(gyroid(p/a)*a);
        a /= 2.0;
    }
    return result;
}

float noise3(vec3 p)
{
    float result = 0.0;
    float a = 0.5;
    for (float i = 0.0; i < 5.0; i += 1.0)
    {
        p.y += result * 0.5 + iTime.x * 0.05;
        result += abs(gyroid(p/a)*a);
        a /= 2.0;
    }
    return result;
}

float noise4(vec3 p)
{
    float result = 0.0;
    float a = 0.5;
    for (float i = 0.0; i < 3.0; i += 1.0)
    {
        p.y += result * 0.5;
        result += abs(gyroid(p/a)*a);
        a /= 2.0;
    }
    return result;
}

float map(vec3 p)
{
    float dist = 100.0;
    
    p.x += 0.7;
    
    p.z -= iTime.x * 0.1;
    
    vec3 q = p;
    
    p.z = mul(0.5, p.z);
    dist = noise(p);
    
    float grid = 0.5;
    float shape = length(repeat(p, grid)) - grid/1.5;
    shape = max(dist, abs(shape) - 0.1);
    dist = max(dist, -abs(shape)*0.5);
    
    p = q * 5.0;
    p.y = mul(0.3, p.y);
    dist += abs(noise(p)) * 0.2;
    
    p = q * 10.0;
    p.y = mul(0.2, p.y);
    dist += mul((abs(noise(p)), 4.0) * 0.1, (abs(noise(p)), 4.0) * 0.1);
    
    p = q;
    p.y += cos(p.z * 2.0) * 0.05;
    p.zx = mul(0.3, p.zx);
    dist -= mul((abs(noise4(p * 10.0)), 4.0) * 0.03, (abs(noise4(p * 10.0)), 4.0) * 0.03);
    
    p = q * 10.0;
    p.z = mul(2.0, p.z);
    dist -= noise2(p) * 0.05;
    
    dist -= 0.1;
    dist -= 0.1 * sin(q.z);
    
    dist -= max(0.0, p.y) * 0.02;
    
    float water = q.y + 1.0 + noise3(q * 2.0) * 0.01;
    
    material = water < dist ? 1.0 : 0.0;
    dist = min(water, dist);
    
    return dist;
}

vec3 getNormal(vec3 pos, float e)
{
    vec2 noff = vec2(e, 0.0);
    return normalize(map(pos) - vec3(
        map(pos - noff.xyy), 
        map(pos - noff.yxy), 
        map(pos - noff.yyx)
    ));
}

vec3 getColor(vec3 pos, vec3 normal, vec3 ray, float shade)
{
    vec3 color = 0.5 + 0.5 * cos(vec3(1.0, 2.0, 3.0) * 5.9 + normal.y - normal.z * 0.5 - 0.5);
    
    color = mul(dot(normal, -normalize(pos)) * 0.5 + 0.5, color);
    
    color = mul(shade * shade, color);
    
    return color;
}

void main()
{
    vec3 color = vec3(0.0, 0.0, 0.0);
    
    vec2 fragCoord = v_texcoord0 * iResolution.xy;
    vec2 uv = fragCoord / iResolution.xy;
    vec2 p = (2.0 * fragCoord - iResolution.xy) / iResolution.y;
    vec3 pos = vec3(0.0, 0.0, 0.0);
    vec3 ray = normalize(vec3(p, -1.0));
    vec3 rng = hash(uvec3(fragCoord, 0.0));
    
    bool clicked = iMouse.x > 0.0;
    bool clicking = iMouse.z > 0.0;
    if (clicked)
    {
        vec2 mouse = iMouse.xy - abs(iMouse.zw) + iResolution.xy / 2.0;
        vec2 angle = (2.0 * mouse - iResolution.xy) / iResolution.y;
        ray.yz = mul(rot(angle.y), ray.yz);
        ray.xz = mul(rot(angle.x), ray.xz);
    }

    total = 0.0;
    float shade = 0.0;
    for (shade = 1.0; shade > 0.0; shade -= 1.0/200.0)
    {
        float dist = map(pos);
        if (dist < 0.001 * total || total > 20.0) break;
        dist = mul(0.12 + 0.05 * rng.z, dist);
        pos += ray * dist;
        total += dist;
    }

    if (shade > 0.01)
    {
        float mat = material;
        vec3 normal = getNormal(pos, 0.003 * total);
        
        if (mat == 0.0)
        {
            color = getColor(pos, normal, ray, shade);
            float spec = mul((dot(-ray, normal) * 0.5 + 0.5, 100.0), (dot(-ray, normal) * 0.5 + 0.5, 100.0));
            
            color += 0.2 * spec * ss(0.5, 0.0, pos.y + 1.0);
        }
        else
        {
            ray = reflect(ray, normal);
            pos += ray * 0.05;
            total = 0.0;
            for (shade = 1.0; shade > 0.0; shade -= 1.0/80.0)
            {
                float dist = map(pos);
                if (dist < 0.05 * total || total > 20.0) break;
                dist = mul(0.2, dist);
                pos += ray * dist;
                total += dist;
            }
            
            color = getColor(pos, getNormal(pos, 0.001), ray, shade);
            //color = mul(ss(1.0, 0.0, pos.y + 1.0), color);
            //color = mul(ss(0.0, 0.6, pos.y + 1.2), color);
        }
    }
    
    gl_FragColor = vec4(ray, 1.0);
}