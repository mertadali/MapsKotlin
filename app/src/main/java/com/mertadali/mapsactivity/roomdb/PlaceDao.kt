package com.mertadali.mapsactivity.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

import com.mertadali.mapsactivity.model.Place
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

// Uygulamanızın veritabanındaki verileri sorgulamak, güncellemek, eklemek ve silmek için kullanabileceği yöntemler sağlayan veri erişim nesneleri (DAO'lar).


@Dao

interface PlaceDao {

   @Query("SELECT * FROM Place")
    fun getAll() : Flowable<List<Place>>


    @Insert
    fun insert(place: Place) : Completable

    @Delete
    fun delete(place: Place) : Completable


}




















