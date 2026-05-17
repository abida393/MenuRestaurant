package com.savoria.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.savoria.app.data.local.OrderSeeder
import com.savoria.app.data.local.UserSeeder
import com.savoria.app.data.local.converter.Converters
import com.savoria.app.data.local.dao.CartDao
import com.savoria.app.data.local.dao.CategoryDao
import com.savoria.app.data.local.dao.ChefOrderDao
import com.savoria.app.data.local.dao.DishDao
import com.savoria.app.data.local.dao.OrderDao
import com.savoria.app.data.local.dao.ReservationDao
import com.savoria.app.data.local.dao.ShiftDao
import com.savoria.app.data.local.dao.TableDao
import com.savoria.app.data.local.dao.UserDao
import com.savoria.app.data.local.entity.CartItemEntity
import com.savoria.app.data.local.entity.Category
import com.savoria.app.data.local.entity.ChefOrder
import com.savoria.app.data.local.entity.Dish
import com.savoria.app.data.local.entity.OrderEntity
import com.savoria.app.data.local.entity.OrderItem
import com.savoria.app.data.local.entity.Reservation
import com.savoria.app.data.local.entity.Shift
import com.savoria.app.data.local.entity.TableEntity
import com.savoria.app.data.local.entity.User
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Shift::class,
        Category::class,
        Dish::class,
        TableEntity::class,
        Reservation::class,
        OrderEntity::class,
        OrderItem::class,
        ChefOrder::class,
        CartItemEntity::class
    ],
    version = 8,
    exportSchema = false

)
@TypeConverters(Converters::class)
abstract class SavoriaDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun dishDao(): DishDao
    abstract fun categoryDao(): CategoryDao
    abstract fun orderDao(): OrderDao
    abstract fun chefOrderDao(): ChefOrderDao
    abstract fun cartDao(): CartDao
    abstract fun tableDao(): TableDao
    abstract fun reservationDao(): ReservationDao
    abstract fun shiftDao(): ShiftDao

    companion object {
        @Volatile
        private var INSTANCE: SavoriaDatabase? = null

        fun getDatabase(context: Context, scope: kotlinx.coroutines.CoroutineScope): SavoriaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SavoriaDatabase::class.java,
                    "savoria_database"
                )
                .addCallback(DatabaseCallback(context.applicationContext, scope))
                .addMigrations(
                    object : Migration(2, 3) {
                    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE dishes ADD COLUMN badgeType TEXT")
                        database.execSQL("UPDATE dishes SET badgeType = NULL")
                        database.execSQL("""
                            CREATE TABLE dishes_new (
                                id TEXT NOT NULL PRIMARY KEY,
                                categoryId TEXT,
                                nom TEXT NOT NULL,
                                description TEXT NOT NULL DEFAULT '',
                                prix REAL NOT NULL,
                                prixFormat TEXT NOT NULL DEFAULT '',
                                photoUrl TEXT NOT NULL,
                                disponible INTEGER NOT NULL,
                                badgeText TEXT,
                                badgeType TEXT,
                                isFavorite INTEGER NOT NULL DEFAULT 0,
                                prixPromo REAL,
                                FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE SET NULL
                            )
                        """)
                        database.execSQL("""
                            INSERT INTO dishes_new SELECT id, categoryId, nom, description, prix, prixFormat,
                            photoUrl, disponible, badgeText, NULL, isFavorite, NULL FROM dishes
                        """)
                        database.execSQL("DROP TABLE dishes")
                        database.execSQL("ALTER TABLE dishes_new RENAME TO dishes")
                        database.execSQL("CREATE INDEX index_dishes_categoryId ON dishes(categoryId)")
                    }
                },
                    object : Migration(3, 4) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL(
                                "ALTER TABLE dishes ADD COLUMN isChefSpecialty INTEGER NOT NULL DEFAULT 0"
                            )
                            database.execSQL(
                                "UPDATE dishes SET isChefSpecialty = 1 WHERE badgeText = 'SPÉCIAL'"
                            )
                        }
                    },
                    object : Migration(4, 5) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL(
                                """
                                CREATE TABLE IF NOT EXISTS chef_orders (
                                    id TEXT NOT NULL PRIMARY KEY,
                                    dishId TEXT NOT NULL,
                                    dishName TEXT NOT NULL,
                                    quantity INTEGER NOT NULL,
                                    price REAL NOT NULL,
                                    status TEXT NOT NULL,
                                    timestamp INTEGER NOT NULL
                                )
                                """.trimIndent()
                            )
                        }
                    },
                    object : Migration(5, 6) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL(
                                """
                                CREATE TABLE IF NOT EXISTS cart_items (
                                    id TEXT NOT NULL PRIMARY KEY,
                                    dishId TEXT NOT NULL,
                                    nom TEXT NOT NULL,
                                    prix REAL NOT NULL,
                                    quantite INTEGER NOT NULL,
                                    sessionId TEXT NOT NULL
                                )
                                """.trimIndent()
                            )
                            database.execSQL(
                                "ALTER TABLE orders ADD COLUMN consommationMode TEXT NOT NULL DEFAULT 'SUR_PLACE'"
                            )
                            database.execSQL(
                                "ALTER TABLE orders ADD COLUMN clientSessionId TEXT NOT NULL DEFAULT ''"
                            )
                            database.execSQL("UPDATE orders SET statut = 'EN_ATTENTE' WHERE statut = 'RECUE'")
                            database.execSQL("UPDATE orders SET statut = 'EN_PREPARATION' WHERE statut = 'EN_CUISINE'")
                            database.execSQL("UPDATE orders SET statut = 'PRET' WHERE statut = 'PRETE'")
                            database.execSQL("UPDATE orders SET statut = 'SERVI' WHERE statut = 'SERVIE'")
                            database.execSQL(
                                "ALTER TABLE chef_orders ADD COLUMN orderId TEXT NOT NULL DEFAULT ''"
                            )
                            database.execSQL(
                                "CREATE INDEX IF NOT EXISTS index_chef_orders_orderId ON chef_orders(orderId)"
                            )
                            database.execSQL(
                                "CREATE INDEX IF NOT EXISTS index_orders_clientSessionId ON orders(clientSessionId)"
                            )
                        }
                    },
                    object : Migration(6, 7) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL(
                                "ALTER TABLE dishes ADD COLUMN isValidatedByAdmin INTEGER NOT NULL DEFAULT 0"
                            )
                            database.execSQL("UPDATE dishes SET isValidatedByAdmin = 1")
                            database.execSQL("ALTER TABLE orders ADD COLUMN excuseMessage TEXT")
                        }
                    },
                    object : Migration(7, 8) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL(
                                "ALTER TABLE order_items ADD COLUMN nomPlat TEXT NOT NULL DEFAULT ''"
                            )
                        }
                    }
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance

            }
        }
    }

    private class DatabaseCallback(
        private val appContext: Context,
        private val scope: kotlinx.coroutines.CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    UserSeeder.ensureStaffAccounts(appContext, database.userDao())
                    populateDatabase(database)
                }
            }
        }

        override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch {
                    UserSeeder.ensureStaffAccounts(appContext, database.userDao())
                    OrderSeeder.seedSampleChefOrders(database)
                }
            }
        }

        suspend fun populateDatabase(database: SavoriaDatabase) {
            val categoryDao = database.categoryDao()
            val dishDao = database.dishDao()
            val categories = listOf(
                Category(id = "Mains", nom = "Plats Principaux", ordreAffichage = 1),
                Category(id = "Seafood", nom = "Fruits de Mer", ordreAffichage = 2),
                Category(id = "Starters", nom = "Entrées", ordreAffichage = 3),
                Category(id = "Desserts", nom = "Desserts", ordreAffichage = 4)
            )
            categories.forEach { categoryDao.insertCategory(it) }

            val dishes = listOf(
                Dish(
                    nom = "Truffle Beef Wellington",
                    categoryId = "Mains",
                    prix = 48.0,
                    prixFormat = "48,00 €",
                    description = "Notre côte de bœuf Angus cuite lentement pendant 48 heures, nappée d'une réduction de Cabernet Sauvignon. Servi sur une polenta de maïs héritage veloutée, garni d'oignons grelots rôtis et de gremolata fraîche.",
                    badgeText = "NOUVEAU",
                    badgeType = "red_small",
                    photoUrl = "dish_wellington",
                    disponible = true,
                    isFavorite = true,
                    isValidatedByAdmin = true
                ),
                Dish(
                    nom = "Saint-Jacques Hokkaido",
                    categoryId = "Seafood",
                    prix = 42.0,
                    prixFormat = "42,00 €",
                    description = "Saint-Jacques de Hokkaido poêlées, parfaitement dorées sur le dessus, servies sur un lit de purée de chou-fleur avec des perles de caviar et des micro-pousses.",
                    badgeText = "SPÉCIAL",
                    badgeType = "blue_small",
                    photoUrl = "dish_scallops",
                    disponible = true,
                    isFavorite = false,
                    isChefSpecialty = true,
                    isValidatedByAdmin = true
                ),
                Dish(
                    nom = "Pappardelle aux Champignons Sauvages",
                    categoryId = "Mains",
                    prix = 30.0,
                    prixFormat = "30,00 €",
                    description = "Larges nouilles aux œufs mélangées avec des cèpes et des chanterelles, riche sauce crème à la truffe, garnie de parmesan râpé et de thym frais.",
                    badgeText = null,
                    badgeType = null,
                    photoUrl = "dish_pappardelle",
                    disponible = true,
                    isFavorite = true,
                    isValidatedByAdmin = true
                ),
                Dish(
                    nom = "Sphère de Lave Valrhona",
                    categoryId = "Desserts",
                    prix = 18.0,
                    prixFormat = "18,00 €",
                    description = "Sphère parfaite de chocolat noir fondant révélant un cœur liquide chaud, boule de glace à la gousse de vanille, décoration à la feuille d'or, crumble au chocolat.",
                    badgeText = "NOUVEAU",
                    badgeType = "red_small",
                    photoUrl = "dish_lava_sphere",
                    disponible = true,
                    isFavorite = true,
                    isValidatedByAdmin = true
                ),
                Dish(
                    nom = "Caprese Burrata Héritage",
                    categoryId = "Starters",
                    prix = 24.0,
                    prixFormat = "24,00 €",
                    description = "Fromage burrata frais et crémeux au centre avec des tomates héritage colorées (rouges, jaunes, vertes), feuilles de basilic frais, filet d'huile d'olive premium, flocons de sel de mer Maldon.",
                    badgeText = null,
                    badgeType = null,
                    photoUrl = "dish_burrata",
                    disponible = true,
                    isFavorite = true,
                    isValidatedByAdmin = true
                ),
                Dish(
                    nom = "Agneau aux Herbes de Provence",
                    categoryId = "Mains",
                    prix = 52.0,
                    prixFormat = "52,00 €",
                    description = "Carré d'agneau en croûte d'herbes de Provence, parfaitement rôti, servi avec des légumes ratatouille et son jus.",
                    badgeText = "SPÉCIAL",
                    badgeType = "blue_small",
                    photoUrl = "dish_lamb",
                    disponible = true,
                    isFavorite = false,
                    isChefSpecialty = true,
                    isValidatedByAdmin = true
                )
            )
            dishes.forEach { dishDao.insertDish(it) }
        }

        suspend fun seedSampleChefOrders(database: SavoriaDatabase) {
            OrderSeeder.seedSampleChefOrders(database)
        }
    }
}
