package ru.otus.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.rules.KotlinCoreEnvironmentTest
import io.gitlab.arturbosch.detekt.test.compileAndLintWithContext
import io.kotest.matchers.collections.shouldHaveSize
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.junit.jupiter.api.Test

@KotlinCoreEnvironmentTest
internal class GlobalScopeRuleTest(private val env: KotlinCoreEnvironment) {
    private val rule = GlobalScopeRule(Config.empty)

    @Test
    fun `no reports launch in CoroutineScope`() {
        val code = """
        import kotlinx.coroutines.GlobalScope
        import kotlinx.coroutines.CoroutineScope
        import kotlinx.coroutines.Dispatchers

        suspend fun loadInfo() {
            CoroutineScope(Dispatchers.Default).launch {
        
            }
        }
        """
        val findings = rule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `no reports async in CoroutineScope`() {
        val code = """
        import kotlinx.coroutines.GlobalScope
        import kotlinx.coroutines.CoroutineScope
        import kotlinx.coroutines.Dispatchers

        suspend fun loadInfo() {
            CoroutineScope(Dispatchers.Default).async {
        
            }
        }
        """
        val findings = rule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `no report launch in coroutineScope`() {
        val code = """
        import kotlinx.coroutines.GlobalScope
        import kotlinx.coroutines.CoroutineScope
        import kotlinx.coroutines.Dispatchers

        suspend fun loadInfo() {
            coroutineScope {
                launch {}
            }
        }
        """
        val findings = rule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }


    @Test
    fun `no report launch in supervisor scope`() {
        val code = """
        import kotlinx.coroutines.GlobalScope
        import kotlinx.coroutines.CoroutineScope
        import kotlinx.coroutines.Dispatchers

        suspend fun loadInfo() {
            supervisorScope {
                launch {}
                async {}
            }
        }
        """
        val findings = rule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 0
    }

    @Test
    fun `reports launch in global scope`() {
        val code = """
        import kotlinx.coroutines.GlobalScope
        import kotlinx.coroutines.CoroutineScope
        import kotlinx.coroutines.Dispatchers
    
        class TestGlobalScopeLaunch : Fragment {
          suspend fun loadInfo() {
            GlobalScope.launch {
              // ...
            }
          }
        }
        """
        val findings = rule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }

    @Test
    fun `reports async in global scope`() {
        val code = """
        import kotlinx.coroutines.GlobalScope
        import kotlinx.coroutines.CoroutineScope
        import kotlinx.coroutines.Dispatchers
    
        class TestGlobalScopeAsync : ViewModel {
          suspend fun loadInfo() {
            GlobalScope.async {
              // ...
            }
          }
        }
        """
        val findings = rule.compileAndLintWithContext(env, code)
        findings shouldHaveSize 1
    }
}
