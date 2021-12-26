package com.example.userdata

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.userdata.data.User
import com.example.userdata.ui.theme.Shapes
import com.example.userdata.ui.theme.UserDataTheme

class MainActivity : ComponentActivity() {

    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Navigation()

        }
    }
}


@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun Navigation() {
    val navController = rememberNavController()
    val userList: MutableState<List<User>> = remember { mutableStateOf(emptyList()) }
    val openDialog = remember { mutableStateOf(false) }
    val viewModel: UserViewModel = viewModel()




    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(route = Screen.Home.route) {
            HomeScreen(userList, navController, openDialog)
        }
        composable(route = Screen.AddUser.route) {
            UserScreen(navController)
        }
        composable(route = Screen.UpdateScreen.route + "/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.IntType
                }
            )) { entry ->
            entry.arguments?.let { UpdateScreen(it.getInt("userId"), navController) }
        }

        composable(route = "alert_dialog" + "/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.IntType
                }
            )) { entry ->
            entry.arguments?.let {
                AlertDialogBox(
                    navController,
                    openDialog = openDialog,
                    userId = it.getInt("userId"),
                    viewModel = viewModel
                )
            }
        }
    }

}

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun HomeScreen(
    userList: MutableState<List<User>>,
    navController: NavController,
    openDialog: MutableState<Boolean>
) {


    val viewModel: UserViewModel = viewModel()
    userList.value = viewModel.getAllUsers()

    UserDataTheme {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background) {
            Scaffold(
                topBar = {
                    ActionBar(
                        icon = Icons.Default.Menu,
                        navController = navController,
                        title = "Home"
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { navController.navigate(Screen.AddUser.route) }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "")
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(userList.value, {user:User -> user.id}) { user ->

                            val dismissState = rememberDismissState(
                                confirmStateChange = {
                                    if(it == DismissValue.DismissedToEnd){
                                        viewModel.deleteUser(user)
                                    }
                                    true
                                }
                            )

                            SwipeToDismiss(state = dismissState, background = {
                                val color = when(dismissState.dismissDirection){
                                    DismissDirection.StartToEnd -> Color.Red
                                    DismissDirection.EndToStart -> Color.Transparent
                                    null -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier.fillMaxSize()
                                        .padding(top = 16.dp, end = 16.dp, start = 16.dp)

                                ){
                                    Card(
                                        modifier = Modifier.fillMaxSize(),
                                        backgroundColor = color

                                    ){
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "", tint = Color.White
                                            ,modifier = Modifier
                                                .wrapContentWidth(align = Alignment.Start)
                                                .padding(start = 16.dp)
                                        )
                                    }


                                }

                            },
                            dismissContent = {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .padding(top = 16.dp, end = 16.dp, start = 16.dp)
                                        .combinedClickable(
                                            onClick = {
                                                navController.navigate(Screen.UpdateScreen.route + "/${user.id}")

                                            }
                                        ),
                                    elevation = 3.dp,


                                    ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    ) {
                                        Text(text = "${user.firstName} ${user.lastName}")
                                    }
                                    Text(
                                        text = user.age.toString(),
                                        modifier = Modifier
                                            .wrapContentWidth(align = Alignment.End)
                                            .wrapContentHeight(align = Alignment.CenterVertically)
                                            .padding(horizontal = 16.dp)
                                    )

                                }
                            },
                            directions = setOf(DismissDirection.StartToEnd)
                            )








                        }
                    }

//                    AlertDialogBox(openDialog = openDialog, userId.value, viewModel )


                }


            }

        }
    }
}

@Composable
fun UserScreen(navController: NavController) {

    val firstNameText = remember { mutableStateOf("") }
    val lastNameText = remember { mutableStateOf("") }
    val ageText = remember { mutableStateOf("") }

    val context: Context = LocalContext.current
    val viewModel: UserViewModel = viewModel()
    Scaffold(
        topBar = {
            ActionBar(
                icon = Icons.Default.ArrowBack,
                navController = navController,
                title = "Add User"
            )
        }
    )
    {
        Column {
            OutlinedTextField(
                value = firstNameText.value, onValueChange = {
                    firstNameText.value = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, end = 16.dp, start = 16.dp),
                label = {
                    Text(text = "First Name")
                }
            )
            OutlinedTextField(
                value = lastNameText.value, onValueChange = {
                    lastNameText.value = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, end = 16.dp, start = 16.dp),
                label = {
                    Text(text = "Last Name")
                }
            )
            OutlinedTextField(
                value = ageText.value, onValueChange = {
                    ageText.value = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, end = 16.dp, start = 16.dp),
                label = {
                    Text(text = "Age")
                }
            )

            OutlinedButton(
                onClick = {
                    if (checkIfEmpty(
                            firstName = firstNameText.value,
                            lastName = lastNameText.value,
                            age = ageText.value
                        )
                    ) {
                        insertToDatabase(
                            viewModel,
                            context,
                            firstNameText.value,
                            lastNameText.value,
                            ageText.value
                        )
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                },
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .align(alignment = Alignment.CenterHorizontally)
            ) {
                Text(text = "Add User", fontSize = 18.sp)
            }

        }

    }
}


@Composable
fun UpdateScreen(userId: Int, navController: NavController) {

    val updatedFirstName = remember { mutableStateOf("") }
    val updatedLastName = remember { mutableStateOf("") }
    val updatedAge = remember { mutableStateOf("") }

    val context = LocalContext.current
    val viewModel: UserViewModel = viewModel()

    Scaffold(topBar = {
        ActionBar(
            icon = Icons.Default.ArrowBack,
            navController = navController,
            title = "Update User"
        )
    }) {
        Column(
        ) {
            OutlinedTextField(value = updatedFirstName.value, onValueChange = {
                updatedFirstName.value = it
            },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, end = 16.dp, start = 16.dp),
                label = {
                    Text(text = "First Name")
                })

            OutlinedTextField(value = updatedLastName.value, onValueChange = {
                updatedLastName.value = it
            },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, end = 16.dp, start = 16.dp),
                label = {
                    Text(text = "Last Name")
                })

            OutlinedTextField(
                value = updatedAge.value,
                onValueChange = {
                    updatedAge.value = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, end = 16.dp, start = 16.dp),
                label = {
                    Text(text = "Age")
                },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {


                OutlinedButton(
                    onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = 16.dp)

                ) {

                    Text(text = "Cancel", fontSize = 18.sp)
                }

                OutlinedButton(
                    onClick = {
                        if (checkIfEmpty(
                                updatedFirstName.value,
                                updatedLastName.value,
                                updatedAge.value
                            )
                        ) {

                            updateUser(
                                userId = userId,
                                context = context,
                                viewModel = viewModel,
                                firstName = updatedFirstName.value,
                                lastName = updatedLastName.value,
                                age = updatedAge.value
                            )
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route)
                            }
                        }

                    },
                    modifier = Modifier
                        .padding(vertical = 16.dp)

                ) {

                    Text(text = "Update", fontSize = 18.sp)
                }
            }


        }
    }


}

@Composable
fun ActionBar(icon: ImageVector, navController: NavController, title: String) {
    TopAppBar(navigationIcon = {
        Icon(icon, "",
            modifier = Modifier
                .clickable {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
                .padding(horizontal = 12.dp))
    },
        title = {
            Text(text = title)
        })
}

@Composable
fun AlertDialogBox(
    navController: NavController,
    openDialog: MutableState<Boolean>,
    userId: Int,
    viewModel: UserViewModel
) {


    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = {
                Text(text = "Delete User")
            },
            text = {
                Column() {
                    Text("Are you sure?")
                }
            },
            buttons = {
                Row(
                    modifier = Modifier
                        .padding(all = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            openDialog.value = false

                            navController.navigate(Screen.Home.route)
                        }
                    ) {
                        Text("No")
                    }
                    Button(
                        onClick = {
                            deleteUser(viewModel, userId)
                            openDialog.value = false
                            navController.navigate(Screen.Home.route)
                        }
                    ) {
                        Text("Yes")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.9f),
            backgroundColor = MaterialTheme.colors.background
        )
    }


}

fun deleteUser(viewModel: UserViewModel, userId: Int) {
    val user = User(userId, "", "", 0)
    viewModel.deleteUser(user)
}

fun checkIfEmpty(
    firstName: String,
    lastName: String,
    age: String
): Boolean {

    return !TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName) && !TextUtils.isEmpty(age)

}

fun insertToDatabase(
    viewModel: UserViewModel,
    context: Context,
    firstName: String,
    lastName: String,
    age: String
) {

    if (checkIfEmpty(firstName, lastName, age)) {
        val user = User(0, firstName, lastName, age.toInt())

        viewModel.addUser(user)
    } else {
        Toast.makeText(context, "Please fill out the forms", Toast.LENGTH_SHORT).show()
    }


}

fun updateUser(
    userId: Int,
    context: Context,
    viewModel: UserViewModel,
    firstName: String,
    lastName: String,
    age: String
) {

    if (checkIfEmpty(firstName, lastName, age)) {
        val user = User(userId, firstName, lastName, age.toInt())

        viewModel.updateUser(user)
    } else {
        Toast.makeText(context, "Please fill out the forms", Toast.LENGTH_SHORT).show()
    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    UserDataTheme {

    }
}