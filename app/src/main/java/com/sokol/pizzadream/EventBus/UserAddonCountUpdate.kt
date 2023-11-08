package com.sokol.pizzadream.EventBus

import com.sokol.pizzadream.Model.AddonModel

class UserAddonCountUpdate(var isSuccess:Boolean, var addon: AddonModel, var pos: Int) {
}