package com.github.kr328.clash.service.remote

import com.github.kr328.clash.service.model.Profile
import com.github.kr328.kaidl.BinderInterface
import kotlin.uuid.Uuid

@BinderInterface
interface IProfileManager {
  suspend fun create(type: Profile.Type, name: String, source: String = ""): Uuid

  suspend fun clone(uuid: Uuid): Uuid

  suspend fun commit(uuid: Uuid, callback: IFetchObserver? = null)

  suspend fun release(uuid: Uuid)

  suspend fun delete(uuid: Uuid)

  suspend fun patch(uuid: Uuid, name: String, source: String, interval: Long)

  suspend fun update(uuid: Uuid)

  suspend fun queryByUUID(uuid: Uuid): Profile?

  suspend fun queryAll(): List<Profile>

  suspend fun queryActive(): Profile?

  suspend fun setActive(profile: Profile)
}
