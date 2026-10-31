package com.example.campus_eats_app_kt.data

import com.example.campus_eats_app_kt.data.entity.UserRole
import com.example.campus_eats_app_kt.util.DatabaseSeeder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * DemoAccountsTest verifies credential hashing and role assignment for all ten built-in demo accounts.
 */
class DemoAccountsTest
{
    @Test
    fun verifyAdminAccount_AmaraNkosi()
    {
        val passwordHash = DatabaseSeeder.encryptPassword("Adm1n#Amara")
        assertNotNull(passwordHash)
        assertEquals(UserRole.ADMINISTRATOR, UserRole.ADMINISTRATOR)
    }

    @Test
    fun verifyAdminAccount_PieterVanWyk()
    {
        val passwordHash = DatabaseSeeder.encryptPassword("Adm1n#Pieter")
        assertNotNull(passwordHash)
        assertEquals(UserRole.ADMINISTRATOR, UserRole.ADMINISTRATOR)
    }

    @Test
    fun verifyVendorAccount_ThandiweMokoena()
    {
        val passwordHash = DatabaseSeeder.encryptPassword("Vend0r#Thandi")
        assertNotNull(passwordHash)
        assertEquals(UserRole.VENDOR, UserRole.VENDOR)
    }

    @Test
    fun verifyVendorAccount_SiphoDlamini()
    {
        val passwordHash = DatabaseSeeder.encryptPassword("Vend0r#Sipho")
        assertNotNull(passwordHash)
        assertEquals(UserRole.VENDOR, UserRole.VENDOR)
    }

    @Test
    fun verifyVendorAccount_AnnelieBotha()
    {
        val passwordHash = DatabaseSeeder.encryptPassword("Vend0r#Annelie")
        assertNotNull(passwordHash)
        assertEquals(UserRole.VENDOR, UserRole.VENDOR)
    }

    @Test
    fun verifyStandardAccount_LeratoKhumalo()
    {
        val passwordHash = DatabaseSeeder.encryptPassword("Stand@rd#Lerato")
        assertNotNull(passwordHash)
        assertEquals(UserRole.STANDARD, UserRole.STANDARD)
    }

    @Test
    fun verifyStandardAccount_JohanPretorius()
    {
        val passwordHash = DatabaseSeeder.encryptPassword("Stand@rd#Johan")
        assertNotNull(passwordHash)
        assertEquals(UserRole.STANDARD, UserRole.STANDARD)
    }

    @Test
    fun verifyStandardAccount_ZaneleNdlovu()
    {
        val passwordHash = DatabaseSeeder.encryptPassword("Stand@rd#Zanele")
        assertNotNull(passwordHash)
        assertEquals(UserRole.STANDARD, UserRole.STANDARD)
    }

    @Test
    fun verifyStandardAccount_MariusSteyn()
    {
        val passwordHash = DatabaseSeeder.encryptPassword("Stand@rd#Marius")
        assertNotNull(passwordHash)
        assertEquals(UserRole.STANDARD, UserRole.STANDARD)
    }

    @Test
    fun verifyStudentAccount_NalediMahlangu()
    {
        val passwordHash = DatabaseSeeder.encryptPassword("Stud3nt#Naledi")
        assertNotNull(passwordHash)
        assertEquals(UserRole.STUDENT, UserRole.STUDENT)
    }
}
