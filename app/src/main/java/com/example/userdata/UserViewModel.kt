package com.example.userdata

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.userdata.data.User
import com.example.userdata.data.UserDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = UserDatabase.getDatabase(application).userDao()

    private val repository: UserRepository = UserRepository(userDao = userDao)
    private val getAllUsers: MutableState<List<User>> = mutableStateOf(listOf())
    /*
    the getAllUsers should be a MutableState of List of User so that we can use it
    in compose without any hassle
     */

    fun getAllUsers(): List<User> {
        viewModelScope.launch {
            repository.getAllUsers.collect {
                getAllUsers.value = it
            }
        }
        return getAllUsers.value
    }

    fun addUser(user: User) {

        viewModelScope.launch {
            repository.addUser(user)
        }
    }

    fun updateUser(user: User){
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }

    fun deleteUser(user:User){
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }
}