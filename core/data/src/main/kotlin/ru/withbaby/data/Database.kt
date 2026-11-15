package ru.withbaby.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "children")
data class Child(@PrimaryKey val id: String, val name: String, val birthday: String, val feeding: String, val needs: String)
@Entity(tableName = "trips")
data class Trip(@PrimaryKey val id: String, val childId: String, val childName: String, val scenario: String, val hours: Int,
 val transport: String, val departure: Long, val created: Long = System.currentTimeMillis(), val completed: Boolean = false,
 val favorite: Boolean = false, val swim: Boolean = false, val sleep: Boolean = false, val weatherNote: String = "Без погодных данных", @ColumnInfo(defaultValue="0") val returnAt: Long = 0)
@Entity(tableName = "items", foreignKeys = [ForeignKey(entity = Trip::class, parentColumns = ["id"], childColumns = ["tripId"], onDelete = ForeignKey.CASCADE)], indices = [Index("tripId")])
data class PackingItem(@PrimaryKey val id: String, val tripId: String, val name: String, val count: Int, val category: String,
 val reason: String, val packed: Boolean = false, val feedback: Int = 0, val imageKey:String?=null, val photo:String?=null)
@Entity(tableName = "bags")
data class Bag(@PrimaryKey val id: String, val name: String)
@Entity(tableName = "stock", foreignKeys = [ForeignKey(entity = Bag::class, parentColumns = ["id"], childColumns = ["bagId"], onDelete = ForeignKey.CASCADE)], indices = [Index("bagId")])
data class Stock(@PrimaryKey val id: String, val bagId: String, val name: String, val count: Int = 1, val minimum: Int = 1, val packed: Boolean = false, val imageKey:String?=null, val photo:String?=null)
@Entity(tableName = "learning", primaryKeys = ["childId", "name"])
data class Learning(val childId: String, val name: String, val delta: Int)
@Entity(tableName = "weather")
data class WeatherCache(@PrimaryKey val key: Int = 1, val city: String, val lat: Double, val lon: Double, val json: String, val fetched: Long)
@Entity(tableName = "reference")
data class Reference(@PrimaryKey val key: Int = 1, val version: Int, val json: String, val checked: Long)

@Entity(tableName="wardrobe", foreignKeys=[ForeignKey(entity=Child::class,parentColumns=["id"],childColumns=["childId"],onDelete=ForeignKey.CASCADE)],indices=[Index("childId")])
data class WardrobeItem(@PrimaryKey val id:String,val childId:String,val catalogId:String?,val name:String,val group:String,val size:String="",val season:String="Любой сезон",val warmth:Int=1,val imageKey:String="body_long",val photo:String?=null)
@Entity(tableName="outfit_choices",primaryKeys=["childId","slot"],foreignKeys=[ForeignKey(entity=Child::class,parentColumns=["id"],childColumns=["childId"],onDelete=ForeignKey.CASCADE)],indices=[Index("childId")])
data class OutfitChoice(val childId:String,val slot:String,val reference:String)

@Dao
interface BabyDao {
 @Query("SELECT * FROM wardrobe ORDER BY name") fun wardrobe():Flow<List<WardrobeItem>>
 @Query("SELECT * FROM outfit_choices") fun outfitChoices():Flow<List<OutfitChoice>>
 @Upsert suspend fun wardrobe(item:WardrobeItem)
 @Upsert suspend fun choice(choice:OutfitChoice)
 @Delete suspend fun deleteWardrobe(item:WardrobeItem)
 @Query("DELETE FROM outfit_choices WHERE childId=:childId") suspend fun resetOutfit(childId:String)
 @Query("DELETE FROM wardrobe") suspend fun clearWardrobe()
 @Query("DELETE FROM outfit_choices") suspend fun clearChoices()

 @Query("SELECT * FROM children ORDER BY name") fun children(): Flow<List<Child>>
 @Query("SELECT * FROM trips ORDER BY created DESC") fun trips(): Flow<List<Trip>>
 @Query("SELECT * FROM items ORDER BY category, name") fun items(): Flow<List<PackingItem>>
 @Query("SELECT * FROM bags ORDER BY name") fun bags(): Flow<List<Bag>>
 @Query("SELECT * FROM stock ORDER BY name") fun stock(): Flow<List<Stock>>
 @Query("SELECT * FROM weather WHERE `key`=1") fun weather(): Flow<WeatherCache?>
 @Query("SELECT * FROM reference WHERE `key`=1") fun reference(): Flow<Reference?>
 @Query("SELECT * FROM items WHERE id=:id") suspend fun item(id: String): PackingItem?
 @Query("SELECT * FROM learning WHERE childId=:child") suspend fun learning(child: String): List<Learning>
 @Query("SELECT * FROM learning") suspend fun allLearning(): List<Learning>
 @Upsert suspend fun child(child: Child)
 @Upsert suspend fun trip(trip: Trip)
 @Upsert suspend fun items(items: List<PackingItem>)
 @Upsert suspend fun bag(bag: Bag)
 @Upsert suspend fun stock(stock: Stock)
 @Upsert suspend fun learning(learning: Learning)
 @Upsert suspend fun weather(weather: WeatherCache)
 @Upsert suspend fun reference(reference: Reference)
 @Delete suspend fun deleteChild(child: Child)
 @Delete suspend fun deleteTrip(trip: Trip)
 @Delete suspend fun deleteItem(item: PackingItem)
 @Delete suspend fun deleteStock(stock: Stock)
 @Delete suspend fun deleteBag(bag: Bag)
 @Query("DELETE FROM children") suspend fun clearChildren()
 @Query("DELETE FROM trips") suspend fun clearTrips()
 @Query("DELETE FROM bags") suspend fun clearBags()
 @Query("DELETE FROM learning") suspend fun clearLearning()
 @Query("DELETE FROM learning WHERE childId=:id") suspend fun clearChildLearning(id: String)
 @Query("DELETE FROM trips WHERE childId=:id") suspend fun clearChildTrips(id: String)
 @Query("DELETE FROM weather") suspend fun clearWeather()
 @Query("DELETE FROM reference") suspend fun clearReference()
}
@Database(entities = [Child::class, Trip::class, PackingItem::class, Bag::class, Stock::class, Learning::class, WeatherCache::class, Reference::class, WardrobeItem::class, OutfitChoice::class], version = 2, exportSchema = true)
abstract class BabyDatabase : RoomDatabase() { abstract fun dao(): BabyDao }
