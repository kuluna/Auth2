package app.kuluna.jp.auth2

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import app.kuluna.jp.auth2.commons.DataBindingAdapter
import app.kuluna.jp.auth2.databinding.ActivityMainBinding
import app.kuluna.jp.auth2.databinding.ListTotpBinding
import app.kuluna.jp.auth2.models.Db
import app.kuluna.jp.auth2.models.Totp

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TotpAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        adapter = TotpAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // TOTP 一覧を取得
        adapter.items = Db.create(this).totpDao().getAll()

        Log.i("data", adapter.items.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    class TotpAdapter(context: Context) : DataBindingAdapter<Totp, ListTotpBinding>(context, R.layout.list_totp) {
        override fun bind(holder: DataBindingViewHolder<ListTotpBinding>, item: Totp) {
            holder.binding.totp = item
        }
    }
}
