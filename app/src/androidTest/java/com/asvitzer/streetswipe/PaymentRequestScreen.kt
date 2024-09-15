package com.asvitzer.streetswipe

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.asvitzer.streetswipe.ui.screen.PaymentRequestComponent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentRequestScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    var context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun paymentRequestScreen_showsUiElements() {
        // Start the screen
        composeTestRule.setContent {
            PaymentRequestComponent(isLoading = false) {}
        }

        // Check if the Image with the content description is displayed
        composeTestRule.onNodeWithContentDescription(context.resources.getString(R.string.payment_content_description))
            .assertIsDisplayed()

        // Check if the Text with payment instructions is displayed
        composeTestRule.onNodeWithText(context.resources.getString(R.string.payment_amount_instructions))
            .assertIsDisplayed()

        // Check if the TextField with the hint is displayed
        composeTestRule.onNodeWithText(context.resources.getString(R.string.payment_hint))
            .assertIsDisplayed()

        // Check if the Button is displayed
        composeTestRule.onNodeWithText(context.resources.getString(R.string.payment_button))
            .assertIsDisplayed()
    }

    @Test
    fun paymentRequestScreen_inputValueAndSubmit() {
        // Start the screen
        composeTestRule.setContent {
            PaymentRequestComponent(isLoading = false) {}
        }

        // Initially, the button should be disabled
        composeTestRule.onNodeWithText(context.resources.getString(R.string.payment_button))
            .assertIsNotEnabled()

        // Input a valid amount into the TextField
        composeTestRule.onNodeWithText(context.resources.getString(R.string.payment_hint))
            .performTextInput("50")

        // Now the button should be enabled
        composeTestRule.onNodeWithText(context.resources.getString(R.string.payment_button))
            .assertIsEnabled()

        // Perform a click on the button
        composeTestRule.onNodeWithText(context.resources.getString(R.string.payment_button))
            .performClick()

        // Optionally, add more assertions to verify further behavior
    }

    @Test
    fun paymentRequestScreen_showsLoadingIndicator() {
        // Start the screen with isLoading = true
        composeTestRule.setContent {
            PaymentRequestComponent(isLoading = true) {}
        }

        // Check if the CircularProgressIndicator is displayed when loading
        composeTestRule.onNodeWithContentDescription("CircularProgressIndicator")
            .assertIsDisplayed()

        // The button should be disabled while loading
        composeTestRule.onNodeWithText(context.resources.getString(R.string.payment_button))
            .assertIsNotEnabled()
    }
}