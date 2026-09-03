package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("مهامي اليومية", appName)
  }

  @Test
  fun `weekly plan generates 7 days starting with Saturday`() {
    val weekInfo = DateUtils.getWeekInfo(0)
    assertNotNull(weekInfo)
    assertEquals(7, weekInfo.days.size)
    assertEquals("السبت", weekInfo.days.first().dayNameAr)
    assertEquals("الجمعة", weekInfo.days.last().dayNameAr)
    assertTrue(weekInfo.isCurrentWeek)
  }
}

