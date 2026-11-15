package ru.withbaby.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2=object:Migration(1,2) {
 override fun migrate(db:SupportSQLiteDatabase) {
  db.execSQL("ALTER TABLE trips ADD COLUMN returnAt INTEGER NOT NULL DEFAULT 0")
  for(table in listOf("items","stock")) {db.execSQL("ALTER TABLE $table ADD COLUMN imageKey TEXT");db.execSQL("ALTER TABLE $table ADD COLUMN photo TEXT")}
  db.execSQL("CREATE TABLE IF NOT EXISTS wardrobe (id TEXT NOT NULL, childId TEXT NOT NULL, catalogId TEXT, name TEXT NOT NULL, `group` TEXT NOT NULL, size TEXT NOT NULL, season TEXT NOT NULL, warmth INTEGER NOT NULL, imageKey TEXT NOT NULL, photo TEXT, PRIMARY KEY(id), FOREIGN KEY(childId) REFERENCES children(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
  db.execSQL("CREATE INDEX IF NOT EXISTS index_wardrobe_childId ON wardrobe(childId)")
  db.execSQL("CREATE TABLE IF NOT EXISTS outfit_choices (childId TEXT NOT NULL, slot TEXT NOT NULL, reference TEXT NOT NULL, PRIMARY KEY(childId,slot), FOREIGN KEY(childId) REFERENCES children(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
  db.execSQL("CREATE INDEX IF NOT EXISTS index_outfit_choices_childId ON outfit_choices(childId)")
 }
}
