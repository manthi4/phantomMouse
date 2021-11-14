android_library(
  name = "activities",
  srcs = glob(["src/main/java/com/example/phantomouse/*.java"]),
  custom_package = "com.example.phantomouse",
  manifest = "src/main/AndroidManifest.xml",
  resource_files = glob(["src/main/res/**"]),
)

android_binary(
    name = "android",
    custom_package = "com.example.phantomouse",
    manifest = "ssrc/main/AndroidManifest.xml",
    resource_files = glob(["src/main/res/**"]),
    deps = [":activities"],
)