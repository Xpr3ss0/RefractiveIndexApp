package com.example.refractiveindexapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.xpr3ss0.scientificplot.ScientificPlot
import dev.xpr3ss0.scientificplot.model.DataSeries
import dev.xpr3ss0.scientificplot.state.PlotManager
import dev.xpr3ss0.scientificplot.state.PlotState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.cos
import kotlin.math.sin

@RunWith(AndroidJUnit4::class)
class PlottingWidgetTest {

    // 1. Create the Compose Test Rule
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testScientificPlot() {
        // 2. Prepare your input data
        val nPoints : Int = 1000
        val start : Double = .00002
        val stop : Double = 23.3
        val step : Double = (stop - start) / (nPoints - 1)
        val testInputXData = List<Double>(1000) { i ->
            start + i * step
        }
        val testInputYData = List<Double>(1000) { i ->
            cos(testInputXData[i])
        }
        val testDataSeries = DataSeries(
            testInputXData,
            testInputYData
        )
        val plotState = PlotState.defaultFromData(testDataSeries)
        val plotManager = PlotManager(plotState)

        // 3. Set the content of the test rule to your composable with the input data
        composeTestRule.setContent {

            val modifier = Modifier
            Column(
                modifier = modifier
                    .padding(30.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center

            ) {
                ScientificPlot(
                    plotManager,
                    modifier = modifier
                )
            }
        }

        // 4. Assert and verify the UI behavior
        // Find the node using its testTag and check if it renders properly
        composeTestRule
            .onNodeWithTag("ScientificPlotTest")
            .assertIsDisplayed()

        val testInputYData2 = List<Double>(1000) { i ->
            sin(testInputXData[i])
        }
        val testDataSeries2 = DataSeries(testInputXData, testInputYData2)
        plotManager.plotState = plotManager.plotState.copy(dataSeries = testDataSeries2)

    }



}