package io.ballerine.kmp.bleandroidapp.screens.navigations

sealed class HomeScreenItems(val route:String){

    /*object WalkThroughScreenItems: MainScreenItems("{pagePosition}/WalkThroughScreen"){
        fun createRoute(pagePosition:Int) = "$pagePosition/WalkThroughScreen"
    }*/
    object HomeScreen: HomeScreenItems("HomeScreen")
    object BluetoothDetailsScreen: HomeScreenItems("BluetoothDetailsScreen")
}
