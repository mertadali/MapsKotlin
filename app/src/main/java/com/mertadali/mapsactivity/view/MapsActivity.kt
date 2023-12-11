package com.mertadali.mapsactivity.view

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.mertadali.mapsactivity.R
import com.mertadali.mapsactivity.databinding.ActivityMapsBinding
import com.mertadali.mapsactivity.model.Place
import com.mertadali.mapsactivity.roomdb.PlaceDao
import com.mertadali.mapsactivity.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener {                             // kullanıcı uzun tıklayarak haritadaki marker atılan konumu almak isterse

    private lateinit var mMap: GoogleMap
private lateinit var binding: ActivityMapsBinding
private lateinit var locationManager: LocationManager          // ->! Konumumuzu ayarlamak ve mevcut konumu alabilmek için kullandık.!
private lateinit var locationListener: LocationListener
private lateinit var activityResultLauncher: ActivityResultLauncher<String>
private lateinit var sharedPreferences: SharedPreferences             // -> !bilinen son konum mevcut konuma eşit olabilir o yüzden bir kereliğine mahsus kaydetmek için kullanacağız.!
private var trackBoolean : Boolean? = null
    private var selectedLatitude : Double? = null
    private var selectedLongitude : Double? = null
    private lateinit var db : PlaceDatabase
    private lateinit var dao : PlaceDao
     private val compositeDisposable = CompositeDisposable()       // internete call atarken kullan at mantığıyla yaklaşır ve belleğin şişmesini engeller.


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

     binding = ActivityMapsBinding.inflate(layoutInflater)
     setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher()

        sharedPreferences = this.getSharedPreferences(" com.mertadali.mapsactivity", MODE_PRIVATE)
        trackBoolean = false
        selectedLongitude = 0.0
        selectedLatitude = 0.0

        db = Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Places").build()

        dao = db.placeDao()


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this@MapsActivity)


        // mevcut konumumuzu kullanabilmek için :

        // Casting
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

      /*  locationListener = LocationListener {
          !  bu şekilde kullanabilirdik ancak locationListener yapısı bir arayüz o yüzden object olarak kullanmak daha doğru olacaktır.   !
        }*/

        locationListener = object : LocationListener{
            override fun onLocationChanged(location: Location) {     // her konum değiştiğinde bize haber vermesi için.
                trackBoolean = sharedPreferences.getBoolean("trackBoolean",false)
                if (trackBoolean == false){
                    val userLocaiton = LatLng(location.latitude,location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocaiton,15f))
                    sharedPreferences.edit().putBoolean("trackBoolean",true).apply()
                }


                // ! sorunlardan ilki haritada başka yerleri gezemiyoruz çünkü onLocationChanged sürekli çağırılacak ve konum tam belli değil marker yok ! bu yüzden son konum  algoritması kullanacağız.

            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                super.onStatusChanged(provider, status, extras)
            }

        }

        if (ContextCompat.checkSelfPermission(this@MapsActivity,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            // izin iste
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MapsActivity,android.Manifest.permission.ACCESS_FINE_LOCATION)){
                // Kullanıcıya izin istediğimizi göstermek için:
                Snackbar.make(binding.root,"Permission needed for location",Snackbar.LENGTH_INDEFINITE).setAction("Permission required!"){
                    // izin iste - registerLauncher
                    activityResultLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

                }.show()

            }else{
                // izin iste - registerLauncher
                activityResultLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }else{
            // izin verildi
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastLocation != null){                                                                                        // ! daha önce uydudan bu konum alınmamış olabilir. !
                val userLastLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLastLocation,15f))
            }
            mMap.isMyLocationEnabled = true
        }


    }


     private fun registerLauncher(){

         activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
             if (it){

                 // izin verildi - permission granted    genede bir izin verilip verilmediğinden emin olmak için.
                 if (ContextCompat.checkSelfPermission(this@MapsActivity,android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED){
                     locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                     val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                     if (lastLocation != null){
                         val userLastLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                         mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLastLocation,15f))
                     }

                 }


             }else{
                 // izin red   - permission denied
                 Toast.makeText(this@MapsActivity,"Permission required to use the app",Toast.LENGTH_LONG).show()
             }

         }

    }

    override fun onMapLongClick(p0: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(p0))

        selectedLatitude = p0.latitude
        selectedLongitude = p0.longitude

    }

    /* !!            Main Thread UI -> Çok yoğun bir işlem yaparsak kullanıcı arayüzünü bloklayabilir o yüzden room ile çalışıyoruz
        ancak safe olduğundan bunu arka planda asec olarak yapmamız gerekiyor.           Default Thread - CPU Intensive yoğun işlemler için.
           IO Thread internet/Database işlemleri daha çok yapılır.  Biz burada IO Thread kullanacağız bunun için Rx Java yada Coroutines yapıları kullanılır.      !!                                                         */

    fun save(view : View){
        if (selectedLatitude != null && selectedLongitude != null){
            val place = Place(binding.placeName.toString(),selectedLatitude!!,selectedLongitude!!)
            // Arka planda yapılan işlemleri takip ederek rxjava kullanımı.

           compositeDisposable.add(
               dao.insert(place)
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(this::handleResponse)          // bu işlem bitince referans vererek handleResponse işlemini yapacak.
           )

        }
    }

    private fun handleResponse(){          // call geldiğinde yapılacak işlem.
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    fun delete(view: View){


    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }


}



















