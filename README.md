# gltf loading

a simple .GLTF loader designed to work with libGDX's default rendering pipeline.

this loader DOES NOT USE PBR and it DOES NOT HIJACK YOUR RENDERING VIA ITS OWN SCENE SYSTEM.
if you want to use PBR, you should probably be looking at glx-gltf.
https://github.com/mgsx-dev/gdx-gltf

this code is as I left it on August 31, 2026.
I'm super demotivated to work on anything libGDX related as I've since switched tools to LWJGL3,
although this was a project I felt I needed to release.

this loader is not currently available on maven central, but I did include a .jar file in github releases to use.

this loader is currently unlikely to receive updates, although you have full permission to fork it into your own thing.

see "core/src/main/java/net/bupy/app/Main.java" for usage details.

# features
- simple .GLTF loading, works natively with libGDX's default AssetManager.
- doesn't hijack rendering, simply returns a libGDX Model class to use with any shader you want.
- clean, small foundation to fork and build from, complete with comments and javadoc

# notes
- this loader was originally created to avoid libGDX's .G3DB and .G3DJ formats, as such:
    - this loader DOES NOT SUPPORT PBR.
    - this loader DOES NOT SUPPORT A SCENE GRAPH SYSTEM.
    
- it also doesn't support animations or mipmapping, as I didn't get around to implementing those before release.
