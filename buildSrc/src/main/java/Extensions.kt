import org.gradle.api.Project
import org.gradle.kotlin.dsl.extra

private const val EXTRA_ROOT_PROJECT_CONFIG = "_extra_root_project_config"

fun Project.configureRootProject(closure: RootProjectConfig.() -> Unit) {
    check(name == rootProject.name) {
        "Trying to call rootProjectConfig {...} in \"$name\" " +
                "Root project config can only be initialized in project's build.gradle.kts"
    }
    rootProject.extra[EXTRA_ROOT_PROJECT_CONFIG] = RootProjectConfig().apply(closure)
}

val Project.rootProjectConfig: RootProjectConfig
    get() {
        require(rootProject.extra.has(EXTRA_ROOT_PROJECT_CONFIG)) {
            "Root project config is not defined. Add rootProjectConfig { ... } to your project's build.gradle.kts"
        }
        return rootProject.extra[EXTRA_ROOT_PROJECT_CONFIG] as RootProjectConfig
    }