package com.mertadali.mapsactivity.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


// Uygulamanızın veritabanındaki tabloları temsil eden veri varlıkları.

/*   Aşağıdaki kod bir Kullanıcı veri varlığını tanımlar. Her User örneği, uygulamanın veritabanındaki kullanıcı tablosundaki bir satırı temsil eder.

       @Entity
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?
)
 */


@Entity
data class Place(
    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "latitude")
    var latitude: Double,

    @ColumnInfo(name = "longitude")
    var longitude : Double){


    @PrimaryKey(autoGenerate = true)
    var id = 0



}