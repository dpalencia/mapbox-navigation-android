package com.mapbox.navigation.qa.view

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.navigation.qa.R
import com.mapbox.navigation.qa.utils.LOCATION_PERMISSIONS_REQUEST_CODE
import com.mapbox.navigation.qa.utils.LocationPermissionsHelper
import com.mapbox.navigation.qa.view.adapters.CategoryListAdapterSupport.categoryListOnBindViewHolderFun
import com.mapbox.navigation.qa.view.adapters.CategoryListAdapterSupport.itemTypeProviderFun
import com.mapbox.navigation.qa.view.adapters.CategoryListAdapterSupport.viewHolderFactory
import com.mapbox.navigation.qa.view.adapters.GenericListAdapter
import com.mapbox.navigation.qa.view.adapters.GenericListAdapterItemSelectedFun
import com.mapbox.navigation.qa.view.adapters.GenericListAdapterSameItemFun
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity(), PermissionsListener {

    private val permissionsHelper = LocationPermissionsHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutManager = LinearLayoutManager(this)
        categoryList.layoutManager = LinearLayoutManager(this)
        categoryList.adapter = GenericListAdapter(
            categoryListOnBindViewHolderFun,
            viewHolderFactory,
            categorySelectedDelegate,
            null,
            categorySameItemFun,
            categorySameItemFun,
            itemTypeProviderFun
        )
        categoryList.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))

        when (LocationPermissionsHelper.areLocationPermissionsGranted(this)) {
            true -> requestPermissionIfNotGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            else -> permissionsHelper.requestLocationPermissions(this)
        }
    }

    override fun onStart() {
        super.onStart()
        val categories = resources.getStringArray(R.array.categories).toList()
        (categoryList.adapter as GenericListAdapter<String, *>).let {
            it.swap(categories)
        }
    }

    private val categorySelectedDelegate: GenericListAdapterItemSelectedFun<String> = { postionAndValue ->
        // todo do something when item clicked
    }

    private val categorySameItemFun: GenericListAdapterSameItemFun<String> = { item1, item2 ->
        item1 == item2
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast
            .makeText(
                this,
                "This app needs location and storage permissions" +
                    "in order to show its functionality.",
                Toast.LENGTH_LONG
            ).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            requestPermissionIfNotGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            Toast.makeText(this, "You didn't grant location permissions.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSIONS_REQUEST_CODE) {
            permissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
        } else {
            when (
                grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                true -> {
                    categoryList.isClickable = true
                }
                else -> {
                    categoryList.isClickable = false
                    Toast.makeText(
                        this,
                        "You didn't grant storage or location permissions.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun requestPermissionIfNotGranted(permission: String) {
        val permissionsNeeded = ArrayList<String>()
        if (ContextCompat
                .checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(permission)
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), 10)
        }
    }
}
