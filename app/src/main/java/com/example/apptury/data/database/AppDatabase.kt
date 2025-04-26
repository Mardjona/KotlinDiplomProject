package com.example.apptury.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.apptury.data.database.dao.PlaceDao
import com.example.apptury.data.database.dao.RouteDao
import com.example.apptury.data.database.dao.UserDao
import com.example.apptury.data.model.Place
import com.example.apptury.data.model.Route
import com.example.apptury.data.model.User
import com.example.apptury.util.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Place::class, Route::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun placeDao(): PlaceDao
    abstract fun routeDao(): RouteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apptury_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Предзаполняем базу данных при первом создании
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                populateDatabase(database.placeDao())
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private suspend fun populateDatabase(placeDao: PlaceDao) {
            // Создаем набор предопределенных мест для России
            val preloadedPlaces = listOf(
                Place(
                    name = "Красная площадь",
                    description = "Главная площадь Москвы, расположенная в центре города между Кремлём и историческим районом Китай-город.",
                    category = "Достопримечательности",
                    latitude = 55.7539,
                    longitude = 37.6208
                ),
                Place(
                    name = "Эрмитаж",
                    description = "Один из крупнейших и самых значительных художественных и культурно-исторических музеев России и мира.",
                    category = "Музеи",
                    latitude = 59.9398,
                    longitude = 30.3146
                ),
                Place(
                    name = "Байкал",
                    description = "Самое глубокое озеро на планете, крупнейший природный резервуар пресной воды.",
                    category = "Природа",
                    latitude = 53.5587,
                    longitude = 108.1650
                ),
                Place(
                    name = "Петергоф",
                    description = "Дворцово-парковый ансамбль на южном берегу Финского залива.",
                    category = "Парки",
                    latitude = 59.8852,
                    longitude = 29.9004
                ),
                Place(
                    name = "Третьяковская галерея",
                    description = "Художественный музей в Москве, одно из крупнейших собраний российского изобразительного искусства в мире.",
                    category = "Музеи",
                    latitude = 55.7416,
                    longitude = 37.6207
                ),
                Place(
                    name = "Мамаев курган",
                    description = "Возвышенность на правом берегу реки Волги в Центральном районе города Волгограда, где расположен памятник-ансамбль «Героям Сталинградской битвы».",
                    category = "Достопримечательности",
                    latitude = 48.7425,
                    longitude = 44.5367
                ),
                Place(
                    name = "Казанский Кремль",
                    description = "Древнейшая часть Казани, комплекс архитектурных, исторических и археологических памятников.",
                    category = "Достопримечательности",
                    latitude = 55.7989,
                    longitude = 49.1055
                ),
                Place(
                    name = "Долина гейзеров",
                    description = "Одно из крупнейших гейзерных полей в мире и единственное в Евразии. Расположено на Камчатке.",
                    category = "Природа",
                    latitude = 54.4343,
                    longitude = 160.1395
                ),
                Place(
                    name = "Нижегородский Кремль",
                    description = "Крепость в историческом центре Нижнего Новгорода и его древнейшая часть.",
                    category = "Достопримечательности",
                    latitude = 56.3286,
                    longitude = 44.0031
                ),
                Place(
                    name = "Сочи Парк",
                    description = "Первый тематический парк развлечений в России, расположенный в Адлерском районе города Сочи.",
                    category = "Парки",
                    latitude = 43.4025,
                    longitude = 39.9587
                )
            )
            
            // Вставляем места в базу данных
            placeDao.insertAll(preloadedPlaces)
        }
    }
} 