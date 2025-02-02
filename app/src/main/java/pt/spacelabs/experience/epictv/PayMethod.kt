package pt.spacelabs.experience.epictv

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class PayMethod : ComponentActivity() {

    private lateinit var creditCardBox: LinearLayout
    private lateinit var giftCardBox: LinearLayout
    private lateinit var btnChoosePayment: Button
    private var selectedPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.paymethod)

        creditCardBox = findViewById(R.id.creditCardBox)
        giftCardBox = findViewById(R.id.giftCardBox)
        btnChoosePayment = findViewById(R.id.btnChosePayment)

        btnChoosePayment.isEnabled = false
        btnChoosePayment.setBackgroundResource(R.drawable.btn_disable)

        creditCardBox.setOnClickListener {
            updateSelection(0)
        }

        giftCardBox.setOnClickListener {
            updateSelection(1)
        }

        val nickname = intent.getStringExtra("nickname")
        val name = intent.getStringExtra("name")
        val telef = intent.getStringExtra("telef")
        val password = intent.getStringExtra("password")
        val planId = intent.getStringExtra("planId")
        val email = intent.getStringExtra("email")

        findViewById<Button>(R.id.btnChosePayment).setOnClickListener {
            when (selectedPosition) {
                0 -> {
                    val intent = Intent(this, AddCreditCard::class.java)
                    intent.putExtra("nickname", nickname)
                    intent.putExtra("name", name)
                    intent.putExtra("telef", telef)
                    intent.putExtra("password", password)
                    intent.putExtra("planId", planId)
                    intent.putExtra("email", email)
                    startActivity(intent)
                }
                1 -> {
                    val intent = Intent(this, AddGiftCard::class.java)
                    intent.putExtra("nickname", nickname)
                    intent.putExtra("name", name)
                    intent.putExtra("telef", telef)
                    intent.putExtra("password", password)
                    intent.putExtra("planId", planId)
                    intent.putExtra("email", email)
                    startActivity(intent)
                }
                else -> null
            }

            val backIcon: ImageView = findViewById(R.id.arrowpageback)
            backIcon.setOnClickListener {
                onBackPressed()
            }
        }
    }

    private fun updateSelection(position: Int) {
        selectedPosition = position

        if (selectedPosition == 0) {
            creditCardBox.setBackgroundResource(R.drawable.plan_selector_border_orange)
            giftCardBox.setBackgroundResource(R.drawable.plan_selector_border_cyan)
        } else if (selectedPosition == 1) {
            creditCardBox.setBackgroundResource(R.drawable.plan_selector_border_cyan)
            giftCardBox.setBackgroundResource(R.drawable.plan_selector_border_orange)
        }

        btnChoosePayment.isEnabled = true
        btnChoosePayment.setBackgroundResource(R.drawable.main_orange)
    }
}
