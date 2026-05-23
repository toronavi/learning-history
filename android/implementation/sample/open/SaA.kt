package com.tororontyo.playground.mock.playground

class SaA1 : AppCompatActivity() {

    private val activityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)

        button.setOnClickListener {
            val intent = Intent(this, SaA2::class.java)
            activityLauncher.launch(intent)
        }
    }
}

class SaA2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sub_screen)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateToSuccess.collect { data ->
                    val intent = Intent(this, SaA3::class.java).apply {
                        putExtra("data", data)
                    }
                    startActivity(intent)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }
}