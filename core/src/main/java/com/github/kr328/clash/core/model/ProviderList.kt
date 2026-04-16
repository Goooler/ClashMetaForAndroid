package com.github.kr328.clash.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize class ProviderList(val data: List<Provider>) : List<Provider> by data, Parcelable
