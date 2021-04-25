package com.example.tadee

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.Spanned
import android.text.style.SuperscriptSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.tadee.fragments.Inp
import com.example.tadee.fragments.Out
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.DataPointInterface
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_inp.*
import kotlinx.android.synthetic.main.fragment_out.*
import org.kotlinmath.Complex
import org.kotlinmath.complex
import kotlin.math.*


class MainActivity : AppCompatActivity() {

    // UI
    var fragmentList : MutableList<Fragment> = ArrayList()
    var labels = listOf<String>("symmetrical", "unsymmetrical")

    /////////////////////////

    // Input Parameters
    var system : String = "" //Type of the system - Symmetrical or Unsymmetrical Spacing (transposed)
    var Dph : Double = 0.0 //Spacing between the phase conductors
    var Dab : Double = 0.0
    var Dbc : Double = 0.0
    var Dca : Double = 0.0
    var nC : Int = 0 //Number of sub-conductors per bundle
    var x : Double = 0.0 //Spacing between the sub-conductors
    var nS : Int = 0 //Number of strands in each sub conductor
    var dS : Double = 0.0 //Diameter of each strand
    var l : Double = 0.0 //Length of the line in km
    var model : String = "" //Model of the line (Short/nominal pi/long)
    var Resistance_per : Double = 0.0 //Resistance of the line per km
    var F : Double = 0.0 //Power Frequency
    var Vrline : Double = 0.0 //Nominal System Voltage
    var Pr : Double = 0.0 //Receiving end load in MW
    var Pfr : Double = 0.0 //Power factor of the receiving end load

    // Calculated Parameters
    var w : Double = 0.0
    var SGMDL : Double = 0.0
    var SGMDC : Double = 0.0
    var Inductance : Double = 0.0
    var Capacitance : Double = 0.0
    var Xl : Double = 0.0
    var Xc : Double = 0.0
    var R : Double = 0.0
    var Ic : Complex = complex(0, 0)
    var Z : Complex = complex(0, 0)
    var Y : Complex = complex(0, 0)
    var Zc : Complex = complex(0, 0)
    var A : Complex = complex(0, 0)
    var B : Complex = complex(0, 0)
    var C : Complex = complex(0, 0)
    var D : Complex = complex(0, 0)
    var VReg : Double = 0.0
    var Ir : Complex = complex(0, 0)
    var Is : Complex = complex(0, 0)
    var Vr : Double = 0.0
    var Vs : Complex = complex(0, 0)
    var PL : Double = 0.0
    var Ps : Double = 0.0
    var eff : Double = 0.0
    var compensation : Double = 0.0
    var theta: Double = 0.0
    var radius: Double = 0.0
    var scA : Double = 0.0
    var scB : Double = 0.0
    var rcA : Double = 0.0
    var rcB : Double = 0.0
    var cR : Double = 0.0

    fun check(text: EditText): Double {
        return if(text.text.isNullOrEmpty()){
            696969.00
        } else{
            text.text.toString().toDouble()
        }
    }

    fun round(num: Complex): Complex {
        return complex(
            String.format("%.3f", num.re).toDouble(),
            String.format("%.3f", num.im).toDouble()
        )
    }

    fun round(num: Double): Double{
        return String.format("%.3f", num).toDouble()
    }

    fun Basic(){
        R = Resistance_per*l
        w = 2*Math.PI*F
        Vr = Vrline/sqrt(3.0)
        theta = acos((Pfr))
        var Irmag = Pr/(3*Vr*Pfr)
        Log.d("test1", theta.toString() + " " + Pfr.toString())
        Ir = complex(Irmag * cos(theta), Irmag * sin(theta))
        var m = (3 + sqrt((12 * nS - 3).toDouble())) / 6 //number of layers
        radius = (2 * m - 1) * dS/2
    }

    fun cSqrt(p: Complex): Complex {
        return complex(sqrt((p.mod + p.re) / 2), sign(p.im) * sqrt((p.mod - p.re) / 2))
    }

    fun MGMD(): Double {
        var MGMD: Double = if (system === "Symmetrical")
            Dph
        else
            (Dab * Dbc * Dca).pow((1 / 3).toDouble())
        return MGMD
    }

    fun SGMD(){
        var y = 0.0
        var r = radius
        when (nC) {
            1 -> y = 0.7788 * r
            2 -> y = 0.7788 * r * x.pow((nC - 1).toDouble())
            3 -> y = 0.7788 * r * x.pow((nC - 1).toDouble())
            4 -> y = 0.7788 * r * x.pow((nC - 1).toDouble()) * sqrt(2.0)
            5 -> y = 0.7788 * r * x.pow((nC - 1).toDouble()) * sqrt(sin(54.0));
            6 -> y = 0.7788 * r * x.pow((nC - 1).toDouble()) * 6;
        }
        SGMDL = y.pow((1 / nC).toDouble())
        Inductance = 2 * 10.0.pow(-4.0) * ln(MGMD() / SGMDL)
        if (l<=80) {
            Capacitance = 0.0
        } else {
            SGMDC = (y / 0.7788).pow((1 / nC).toDouble())
            Capacitance = (2 * Math.PI * 8.854187817 * 10.0.pow(-9.0)) / ln(MGMD() / SGMDC)
        }

    }

    fun Charging_Current(): Complex {
        var Ict = complex(0, 0)
        if(model == "Short")
            Ict = complex(0, 0)
        else if(model == "Medium")
            Ict = complex(1, 0) /Y * 2 * Vr
        else if(model == "Long")
            Ict = C * Vr
        return Ict
    }

    fun ABCD(){
        if(model == "Short"){
            A= complex(1, 0)
            B= complex(R, Xl)
            C= complex(0, 0)
            D= A
        }
        else if(model == "Medium"){
            Z = complex(R, Xl)
            Y = complex(0, 1 / Xc)
            A = Z*Y/2 + 1
            B = Z
            C = Y+Z*Y*Y/4
            D = A
        }
        else if(model == "Long"){
            Z = complex(R, Xl)
            Y = complex(0, 1 / Xc)
            Zc = cSqrt(Z / Y)

            var t = cSqrt(Z * Y)
            A = complex(cosh(t.re) * cos(t.im), sinh(t.re) * sin(t.im))
            B = complex(sinh(t.re) * cos(t.im), cosh(t.re) * sin(t.im)) *Zc
            C = complex(sinh(t.re) * cos(t.im), cosh(t.re) * sin(t.im)) /Zc
            D = A
        }
    }

    fun comp(){
        if(model == "Short"){
            var c1 = (A.mod * Vr.pow(2.0) * cos(B.arg))/B.mod
            var c2 = (A.mod * Vr.pow(2.0) * sin(B.arg))/B.mod
            var r = Vr.pow(2.0)/(B.mod*1000000)
            var Qr= sqrt(r.pow(2.0) - (Pr / (3 * 10.0.pow(6.0)) + c1 / 10.0.pow(6.0)).pow(2.0)) - c2/ 10.0.pow(
                6.0
            )
            var Q = -1*tan(acos(Pfr))*Pr/(10.0.pow(6.0))/3
            compensation = Qr - Q
        }
    }

    fun Circle_Diagram() {
        scA = (D.mod* Vs.mod.pow(2.0) * cos(B.arg - D.arg))/(B.mod * 10.0.pow(6.0))
        scB = (D.mod* Vs.mod.pow(2.0) * sin(B.arg - D.arg))/(B.mod * 10.0.pow(6.0))
        rcA = -1*(A.mod* Vs.mod.pow(2.0) * cos(B.arg - A.arg))/(B.mod * 10.0.pow(6.0))
        rcB = -1*(A.mod* Vs.mod.pow(2.0) * sin(B.arg - A.arg))/(B.mod * 10.0.pow(6.0))
        cR = Vs.mod*Vr/(B.mod * 10.0.pow(6.0))
    }

    private fun setGraphSending() {
        var series = LineGraphSeries<DataPointInterface>()
        var series2 = LineGraphSeries<DataPointInterface>()
        val h: Double = scA
        val r = cR
        val k: Double = scB
        var x = h - r
        Log.d("Recieving", " radius=$cR h=$h k=$k")
        while (x <= h + r) {
            val temp = Math.sqrt(r * r - Math.pow(x - h, 2.0))
            val y = temp + k
            val y2 = k - temp
            series.appendData(DataPoint(x, y), false, 17000)
            series2.appendData(DataPoint(x, y2), false, 17000)
            x += cR / 5000
        }
        series.color = Color.RED
        series2.color = Color.RED
        sendingPower.getViewport().setMinX((Math.abs(h) + Math.abs(cR)) * -3)
        sendingPower.getViewport().setMaxX((Math.abs(h) + Math.abs(cR)) * 3)
        sendingPower.getViewport().setMinY((Math.abs(h) + Math.abs(cR)) * -3)
        sendingPower.getViewport().setMaxY((Math.abs(h) + Math.abs(cR)) * 3)
        sendingPower.getViewport().setYAxisBoundsManual(true)
        sendingPower.getViewport().setXAxisBoundsManual(true)
        sendingPower.setTitle("Sending end circle diagram")
        sendingPower.setTitleTextSize(52f)
        sendingPower.computeScroll()
        sendingPower.removeAllSeries()
        sendingPower.addSeries(series)
        sendingPower.addSeries(series2)
        val gridLabel: GridLabelRenderer = sendingPower.getGridLabelRenderer()
        gridLabel.horizontalAxisTitle = "Ps (MW)"
        gridLabel.verticalAxisTitle = "Qs (MVAR)"
    }

    private fun setGraphRecieving() {
        var series = LineGraphSeries<DataPointInterface>()
        var series2 = LineGraphSeries<DataPointInterface>()
        val h: Double = rcA
        val r = cR
        val k: Double = rcB
        var x = h - r
        while (x <= h + r) {
            val temp = Math.sqrt(r * r - Math.pow(x - h, 2.0))
            val y = temp + k
            val y2 = k - temp
            series.appendData(DataPoint(x, y), false, 17000)
            series2.appendData(DataPoint(x, y2), false, 17000)
            x += cR / 5000
        }
        series.color = Color.GREEN
        series2.color = Color.GREEN
        receivingPower.getViewport().setMinX((Math.abs(h) + Math.abs(cR)) * -3)
        receivingPower.getViewport().setMaxX((Math.abs(h) + Math.abs(cR)) * 3)
        receivingPower.getViewport().setMinY((Math.abs(h) + Math.abs(cR)) * -3)
        receivingPower.getViewport().setMaxY((Math.abs(h) + Math.abs(cR)) * 3)
        receivingPower.getViewport().setYAxisBoundsManual(true)
        receivingPower.getViewport().setXAxisBoundsManual(true)
        receivingPower.setTitle("Receiving end circle diagram")
        receivingPower.setTitleTextSize(52f)
        receivingPower.computeScroll()
        receivingPower.removeAllSeries()
        receivingPower.addSeries(series)
        receivingPower.addSeries(series2)
        val gridLabel: GridLabelRenderer = receivingPower.getGridLabelRenderer()
        gridLabel.horizontalAxisTitle = "Pr (MW)"
        gridLabel.verticalAxisTitle = "Qr (MVAR)"
    }


    fun calculate(){
        // Input
        if (toggleButtonGroup.checkedButtonId == com.example.tadee.R.id.sym){
            system = "Symmetrical"
            Dph = check(D1)
        }
        else{
            system = "Unsymmetrical"
            Dab = check(D1)
            Dbc = check(D2)
            Dca = check(D3)
        }
        nC = check(nCtext).toInt()
        x = check(xtext)
        nS = check(nStext).toInt()
        dS = check(dStext)
        l = check(ltext)
        model = modeltext.text.toString()
        Resistance_per = check(resistance)
        F = check(Freq)
        Vrline = (check(Vrtext))
        Pr = (check(Prtext))
        Pfr = check(Pftext)

        if(Dph == 696969.00||Dab == 696969.00||Dbc == 696969.00||nC == 696969||x == 696969.00||nS == 696969||l == 696969.00||model.isNullOrEmpty()||Resistance_per == 696969.00||F == 696969.00||Vrline == 696969.00||Pr == 696969.00||Pfr == 696969.00){
            Toast.makeText(this, "Please Enter Correct Values", Toast.LENGTH_SHORT).show()
            Dph = 0.0
            Dab = 0.0
            Dbc = 0.0
            Dca = 0.0
        }
        else{
            Vrline *= 1000
            Pr *= 1000000
            //Calc
            Basic()

            // 1. Inductance and capacitance per phase per km in H/km
            SGMD()

            // 2. Inductive reactance of the line in Ohm.
            Xl = w * Inductance * l

            // 3. Capacitive reactance of the line in Ohm.
            Xc = if (l <= 80){
                0.0
            } else {
                1 / (w * Capacitance * l)
            }

            // 4. Calculate ABCD parameters of the line.
            ABCD()

            // 5. Calculate the sending end voltage in kV, if the receiving end voltage is maintained at nominal system voltage.
            Vs = (A * Vr + B * Ir)

            // 6. Calculate the sending end current in A.
            Is = (C * Vr + D * Ir)

            // 7. Charging current drawn from the sending end substation
            Ic = Is - Ir

            // 8. Calculate the percentage voltage regulation.
            VReg = (((Vs.mod / A.mod - Vr)) / Vr)*100

            // 9. Calculate the power loss in the line in MW.
            Ps = 3 * Vs.mod * Is.mod * cos(atan(Vs.im / Vs.re) - atan(Is.im / Is.re))
            PL = Ps - Pr

            // 10. Calculate the transmission efficiency.
            eff = (Pr / Ps) * 100

            // 11. Compensation.
            comp()

            // 12. Power Circle Diagram
            Circle_Diagram()

            viewPager.setCurrentItem(1, true)
            Handler().postDelayed({
                if (viewPager.currentItem == 1) {
                    scrollView.visibility = View.VISIBLE
                    setGraphRecieving()
                    setGraphSending()
                    var outputText = "Inductance per phase: ${round(Inductance * 1000)} x 10^-3 H/km\n" +
                            "Capacitance per phase: ${round(Capacitance * 1000000)} x 10^-6 F/km\n" +
                            "Inductive reactance: ${round(Xl)} Ohm\n" +
                            "Capacitive reactance: ${round(Xc)} Ohm\n" +
                            "Charging current: ${round(Ic).asString()} A\n\n" +
                            "ABCD parameters: \n" +
                            "A: ${round(A).asString()} \n" +
                            "B: ${round(B).asString()} \n" +
                            "C: ${round(C).asString()} \n" +
                            "D: ${round(D).asString()} \n\n" +
                            "Sending end voltage: ${round(Vs / 1000).asString()} kV\n" +
                            "Sending end current: ${round(Is).asString()} A\n\n" +
                            "Percentage voltage regulation: ${round(VReg)} %\n" +
                            "Power loss in the line in MW: ${round(PL / 1000000)} MW\n" +
                            "Transmission efficiency: ${round(eff)} %"
                    if (model == "Short") {
                        outputText += "\n\n Compensation: ${round(compensation)} MVAR"
                        if (compensation > 0) {
                            outputText += "\n\n Capacitive compensation \n Use \"Shunt capacitor\" type compensation to avoid undervoltage conditions"
                        } else {
                            outputText += "\n\n Inductive compensation \n Use \"Shunt reactor\" type compensation to avoid overvoltage conditions"
                        }
                    }
                    Output.text = outputText
                }
            }, 100)
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.tadee.R.layout.activity_main)

        val adapter = MyViewPagerAdapter(supportFragmentManager)
        fragmentList.add(Inp())
        fragmentList.add(Out())
        adapter.addFragment(fragmentList, labels)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 2
        tabs.setupWithViewPager(viewPager)

        Handler().postDelayed({
            if (viewPager.currentItem == 0) {
                next.setOnClickListener {
                    calculate()
                }
                reset.setOnClickListener {
                    D1.text.clear()
                    D2.text.clear()
                    D3.text.clear()
                    nCtext.text.clear()
                    xtext.text.clear()
                    nStext.text.clear()
                    dStext.text.clear()
                    ltext.text.clear()
                    modeltext.text.clear()
                    resistance.text.clear()
                    Freq.text.clear()
                    Vrtext.text.clear()
                    Prtext.text.clear()
                    Pftext.text.clear()
                }
            }
        }, 1000)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    next.setOnClickListener {
                        calculate()
                    }

                    reset.setOnClickListener {
                        D1.text.clear()
                        D2.text.clear()
                        D3.text.clear()
                        nCtext.text.clear()
                        xtext.text.clear()
                        nStext.text.clear()
                        dStext.text.clear()
                        ltext.text.clear()
                        modeltext.text.clear()
                        resistance.text.clear()
                        Freq.text.clear()
                        Vrtext.text.clear()
                        Prtext.text.clear()
                        Pftext.text.clear()
                    }

                }

            }

        })



    }



    class MyViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager){

        private var fragmentList : MutableList<Fragment> = ArrayList()
        private var labelsList = listOf<String>()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        fun addFragment(fragment: MutableList<Fragment>, labels: List<String>){
            fragmentList = fragment
            labelsList = labels
        }


        override fun getPageTitle(position: Int): CharSequence? {
            return ""
        }

    }
}