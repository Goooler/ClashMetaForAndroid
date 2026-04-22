package com.github.kr328.clash

import com.github.kr328.clash.common.util.intent
import com.github.kr328.clash.design.ProvidersDesign
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.util.showExceptionToast
import com.github.kr328.clash.util.withClash
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

class ProvidersActivity : BaseActivity<ProvidersDesign>() {
  override suspend fun main() {
    val providers = withClash { queryProviders().sorted() }
    val design = ProvidersDesign(this, providers)

    setContentDesign(design)

    while (isActive) {
      select<Unit> {
        events.onReceive {
          when (it) {
            Event.ProfileLoaded -> {
              val newList = withClash { queryProviders().sorted() }

              if (newList != providers) {
                startActivity(ProvidersActivity::class.intent)

                finish()
              }
            }
            else -> Unit
          }
        }
        design.requests.onReceive {
          when (it) {
            is ProvidersDesign.Request.Update -> {
              launch {
                try {
                  withClash { updateProvider(it.provider.type, it.provider.name) }

                  design.notifyChanged(it.index)
                } catch (e: Exception) {
                  design.showExceptionToast(
                    getString(R.string.format_update_provider_failure, it.provider.name, e.message)
                  )

                  design.notifyUpdated(it.index)
                }
              }
            }
          }
        }
      }
    }
  }
}
