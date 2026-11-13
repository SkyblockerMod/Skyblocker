# Building Skyblocker for Minecraft 26.3

This port is based on [Skyblocker main dc1176a7523f7065c5dcbe30e79c4452402ddc71](https://github.com/SkyblockerMod/Skyblocker/tree/dc1176a7523f7065c5dcbe30e79c4452402ddc71). It uses Java 25, Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3 and Fabric Language Kotlin 1.14.1+kotlin.2.4.20. Use the committed Gradle wrapper.

Native SDL input, RenderPearl extraction/buffers, model/texture submission and changed vanilla hooks use the real 26.3 APIs. Map texture uploads occur at `GameRenderer.render` HEAD, before the native pass opens, retaining the original pixel update behavior. The pinned upstream already excluded the optional EMI source and commented its dependencies; that build scope is retained.

## Separate dependency builds

Choose external directories for `JDK8`, `JDK25`, `MOUL_MAVEN`, `PORT_DEPENDENCIES` and `RENDER_CHEST_MAVEN`. They need not be inside this checkout. Keep each dependency in its own original upstream source checkout with the reviewed port patch applied; child source copies are not part of the Skyblocker contribution.

Build the reviewed MoulConfig v4 port first, based on [NotEnoughUpdates/MoulConfig bd7b9aa3dbf7cef428280daf0156cb5e06338530](https://github.com/NotEnoughUpdates/MoulConfig/tree/bd7b9aa3dbf7cef428280daf0156cb5e06338530). Common compilation requires a genuine Java 8 compiler; modern 26.3 uses Java 25. From its source root:

```sh
export JAVA_HOME="$JDK25"
./gradlew --no-daemon --max-workers=2 --configure-on-demand \
  :modern:modern-26.3:remapJar :modern:modern-26.3:check \
  :modern:modern-26.3:publishMavenPublicationToPortDependenciesRepository \
  -Dmoulconfig.portVersion=4.7.2-codex26.3.1 \
  -Porg.gradle.java.installations.paths="$JDK8,$JDK25" \
  -PportDependencyRepository="$MOUL_MAVEN"
```

This publishes `org.notenoughupdates.moulconfig:modern-26.3:4.7.2-codex26.3.1`. Follow the separate Dandelion `PORTING_26_3.md` to publish `net.azureaaron:dandelion:1.0.0-alpha.22+26.3`, consuming `MOUL_MAVEN` and publishing to `PORT_DEPENDENCIES`. Follow the separate Legacy Item DFU note to publish `net.azureaaron:legacy-item-dfu:1.0.4+26.3` to the same chosen repository.

RenderChest needs no new source contribution. The official Maven 26.3 coordinate was not yet published when this port was prepared. The original [AzureAaron/RenderChest source 0512b13ae5ee780b82a0be80535a8856ac8431f8](https://github.com/AzureAaron/RenderChest/tree/0512b13ae5ee780b82a0be80535a8856ac8431f8) can be built and published to a selected external directory using its own wrapper:

```sh
export JAVA_HOME="$JDK25"
./gradlew --no-daemon --max-workers=2 build publishMavenJavaPublicationToMavenLocal \
  -Dmaven.repo.local="$RENDER_CHEST_MAVEN"
```

`RENDER_CHEST_MAVEN` must be an absolute directory for that Maven-local override. This is a local publication task. A fresh source build need not match the publisher's ZIP hash; preserve its provenance and verify native 26.3 metadata.

The tested unchanged publisher CI JAR came from [run 35052254428](https://github.com/AzureAaron/RenderChest/actions/runs/35052254428), artifact 10429656413: archive SHA256 `52dd5f9ea0f792aa2c5b5a038431cffbe0312011fc0fcb5491304150a05a6521`, contained `render-chest-1.0.3+26.3.jar` SHA256 `6df5281fd4d89f236281468b9612c84a7e53df72d7648df42fb5189925ee434e`. Retain its original Apache 2.0 license. CI artifacts can expire; these hashes identify this build rather than a future official Maven release. Never substitute a 26.2 binary by changing its metadata.

## Production build and checks

From this source root:

```sh
export JAVA_HOME="$JDK25"
./gradlew --no-daemon --max-workers=2 build \
  -PrenderChestPortRepository="$RENDER_CHEST_MAVEN" \
  -PportDependencyRepository="$PORT_DEPENDENCIES"
```

Both properties are optional Maven repositories for the original AzureAaron group; official repository defaults remain. Relative paths resolve against this checkout, while an absolute external directory or file URI makes the layout independent of the source location. No account, owner configuration, DevAuth or game launch is required to build. The production artifact is under `build/libs`; select the unclassified `skyblocker-6.10.4+26.3.jar`, not the sources/development JAR.

Production/checkstyle and all 494 existing tests passed. Three custom shader stages also passed native shaderc compilation and SPIR-V validation. Isolated native mixin and real-GPU fresh-world validation passed separately. Those checks do not establish live Hypixel feature behavior, every optional integration, Vulkan runtime or full-pack compatibility; combined-pack release review remains separate.

The LGPL 3.0-or-later license and original source headers are retained. Dependency source changes belong to their separate repositories and patches.
