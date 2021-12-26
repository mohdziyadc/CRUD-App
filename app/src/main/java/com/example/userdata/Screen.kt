package com.example.userdata

sealed class Screen(val route:String){

    object Home:Screen("home_screen")
    object AddUser:Screen("add_screen")
    object UpdateScreen:Screen("update_screen")
}
