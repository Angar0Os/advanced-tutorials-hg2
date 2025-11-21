import math

import harfang as hg

hg.InputInit()
hg.WindowSystemInit()

res_x, res_y = 1280, 720

win = hg.NewWindow("Shadertoy to Harfang tutorial", res_x, res_y, 32)
hg.RenderInit(win, hg.RT_Direct3D11) #You can add RT_Direct3D11 or RT_DirectD3D12 or RT_OpenGL to specify render API
hg.RenderReset(res_x, res_y, hg.RF_MSAA4X | hg.RF_MaxAnisotropy)

quad_uniform_set_texture_list = hg.UniformSetTextureList()

hg.AddAssetsFolder('resources_compiled_dx')

vtx_layout = hg.VertexLayoutPosFloatTexCoord0UInt8()
plane_mdl = hg.CreatePlaneModel(vtx_layout, 1, 9/16, 1, 1)

plane_mdl_ui = hg.CreatePlaneModel(vtx_layout, 1, 1, 1, 1)


texture, texture_info = hg.LoadTextureFromAssets("textures/noise.png", hg.TF_WClamp)
shader_Dvd = hg.LoadProgramFromAssets('shaders/dvdBounce')

shader_dacingCubes = hg.LoadProgramFromAssets('shaders/shadertoyDust')

shader_text = hg.LoadProgramFromAssets('shaders/texture')

font = hg.LoadFontFromAssets('font/default.ttf', 32)
font_program = hg.LoadProgramFromAssets('core/shader/font.vsb', 'core/shader/font.fsb')

text_uniform_values = [hg.MakeUniformSetValue('u_color', hg.Vec4(1, 1, 0.5))]
text_render_state = hg.ComputeRenderState(hg.BM_Alpha, hg.DT_Always, hg.FC_Disabled)

view_id = 0

qr_flopine = hg.LoadTextureFromAssets("pictures/Cubes_Are_Dancing.png", hg.TF_WClamp)
qr_tdhooper = hg.LoadTextureFromAssets("pictures/DvdBounce.png", hg.TF_WClamp)

while not hg.ReadKeyboard().Key(hg.K_Escape) and hg.IsWindowOpen(win):
    dt = hg.TickClock()
    view_id = 0

    val_uniforms = [hg.MakeUniformSetValue("iTime", hg.Vec4(hg.time_to_sec_f(hg.GetClock()), 0.0, 0.0, 0.0)),
                    hg.MakeUniformSetValue("iResolution", hg.Vec4(res_x, res_y, 0.0, 0.0)),
                    hg.MakeUniformSetValue("iMouse", hg.Vec4(0.0, 0.0, 0.0, 0.0)),
                    hg.MakeUniformSetValue("iChannelResolution", hg.Vec4(texture_info.width, texture_info.height, 0.0, 0.0))]

    text_val_uniforms = [hg.MakeUniformSetValue("color", hg.Vec4(1.0, 1.0 , 1.0, 1.0))]

    #Creating viewport to display the shader.
    viewpoint = hg.TranslationMat4(hg.Vec3(0, 0, -1))
    hg.SetViewPerspective(0, 0, 0, res_x, res_y, viewpoint)

    hg.DrawModel(view_id, plane_mdl, shader_Dvd, val_uniforms, [hg.MakeUniformSetTexture("s_tex", texture, 0)],
                 hg.TransformationMat4(hg.Vec3(-0.5, 0.25, 0), hg.Vec3(-math.pi / 2, 0.0, 0.0)))
    hg.DrawModel(view_id, plane_mdl, shader_dacingCubes, val_uniforms, [hg.MakeUniformSetTexture("s_tex", texture, 0)],
                 hg.TransformationMat4(hg.Vec3(0.5, 0.25, 0), hg.Vec3(-math.pi / 2, 0.0, 0.0)))

    hg.DrawModel(view_id, plane_mdl_ui, shader_text, text_val_uniforms, [hg.MakeUniformSetTexture("s_tex", qr_tdhooper[0], 0)],
                 hg.TransformationMat4(hg.Vec3(-0.15, -0.15, 0), hg.Vec3(-math.pi / 2, 0.0, 0.0), hg.Vec3(0.15, 0.15, 0.15)))


    hg.DrawModel(view_id, plane_mdl_ui, shader_text, text_val_uniforms, [hg.MakeUniformSetTexture("s_tex", qr_flopine[0], 0)],
                 hg.TransformationMat4(hg.Vec3(0.85, -0.15, 0), hg.Vec3(-math.pi / 2, 0.0, 0.0),  hg.Vec3(0.15, 0.15, 0.15)))

    view_id += 1

    hg.SetView2D(view_id, 0, 0, res_x, res_y, -1, 1, hg.CF_Depth, hg.Color.Black, 1, 0)

    hg.DrawText(view_id, font, 'tdhooper - DVD Bounce', font_program, 'u_tex', 0, hg.Mat4.Identity,
                hg.Vec3(140, res_y - 280, 0), hg.DTHA_Left, hg.DTVA_Bottom, text_uniform_values, [], text_render_state)

    hg.DrawText(view_id, font, 'Leon Denise - Cavernic', font_program, 'u_tex', 0, hg.Mat4.Identity,
                hg.Vec3(750, res_y - 280, 0), hg.DTHA_Left, hg.DTVA_Bottom, text_uniform_values, [], text_render_state)

    hg.DrawText(view_id, font, 'SHADERTOY TO HARFANG PORTING', font_program, 'u_tex', 0, hg.Mat4.Identity,
                hg.Vec3((res_y / 2) + 50, res_y - 120, 0), hg.DTHA_Left, hg.DTVA_Bottom, text_uniform_values, [], text_render_state)

    hg.Frame()
    hg.UpdateWindow(win)

hg.RenderShutdown()

