package com.phed.kohistan

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import java.util.UUID

private const val HELPLINE = "03439398790"
private val Navy = Color(0xFF063B73)
private val Green = Color(0xFF0B6B45)

data class Complaint(val id:String, val category:String, val detail:String, val area:String, val status:String, val createdAt:Long)

class MainActivity : ComponentActivity() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PHEDApp() }
    }

    @Composable private fun PHEDApp() {
        var tab by remember { mutableIntStateOf(0) }
        MaterialTheme(colorScheme = lightColorScheme(primary = Navy, secondary = Green)) {
            Scaffold(bottomBar = {
                NavigationBar {
                    NavigationBarItem(tab == 0, { tab = 0 }, label = { Text("شکایت") }, icon = {})
                    NavigationBarItem(tab == 1, { tab = 1 }, label = { Text("میری شکایات") }, icon = {})
                    NavigationBarItem(tab == 2, { tab = 2 }, label = { Text("رابطہ") }, icon = {})
                }
            }) { padding -> Box(Modifier.padding(padding).fillMaxSize()) {
                when (tab) { 0 -> ComplaintForm(); 1 -> MyComplaints(); else -> Contact() }
            }}
        }
    }

    @Composable private fun ComplaintForm() {
        var name by remember { mutableStateOf("") }
        var mobile by remember { mutableStateOf("") }
        var area by remember { mutableStateOf("") }
        var detail by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Water Supply") }
        var message by remember { mutableStateOf("") }
        var lat by remember { mutableStateOf<Double?>(null) }
        var lng by remember { mutableStateOf<Double?>(null) }
        val context = LocalContext.current
        val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true || grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener { location ->
                    if (location != null) { lat = location.latitude; lng = location.longitude }
                }
            }
        }
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("پبلک ہیلتھ انجینئرنگ ڈویژن کوہستان اپر", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Navy); Text("Public Health Engineering Division Kohistan Upper") }
            item { Text("شکایت درج کریں", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
            item { OutlinedTextField(name, { name = it }, label = { Text("نام") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(mobile, { mobile = it }, label = { Text("موبائل نمبر") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(area, { area = it }, label = { Text("علاقہ / مقام") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(category, { category = it }, label = { Text("شکایت کی قسم") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(detail, { detail = it }, label = { Text("شکایت کی تفصیل") }, modifier = Modifier.fillMaxWidth().height(140.dp)) }
            item { Button(onClick = { locationLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }, modifier = Modifier.fillMaxWidth()) { Text(if (lat == null) "📍 موجودہ مقام شامل کریں" else "✓ مقام شامل ہو گیا") } }
            item { Button(enabled = name.isNotBlank() && mobile.isNotBlank() && detail.isNotBlank(), onClick = {
                if (auth.currentUser == null) auth.signInAnonymously().addOnSuccessListener { submit(name,mobile,area,category,detail,lat,lng) { message = it } }
                else submit(name,mobile,area,category,detail,lat,lng) { message = it }
            }, modifier = Modifier.fillMaxWidth()) { Text("شکایت جمع کریں") } }
            item { OutlinedButton(onClick = { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$HELPLINE"))) }, modifier = Modifier.fillMaxWidth()) { Text("0343 9398790 پر فوری کال کریں") } }
            if (message.isNotBlank()) item { Card(Modifier.fillMaxWidth()) { Text(message, Modifier.padding(16.dp), fontWeight = FontWeight.Bold) } }
        }
    }

    private fun submit(name:String,mobile:String,area:String,category:String,detail:String,lat:Double?,lng:Double?,done:(String)->Unit) {
        val uid = auth.currentUser?.uid ?: return done("براہ کرم دوبارہ کوشش کریں")
        val id = "PHED-${UUID.randomUUID().toString().substring(0,8).uppercase()}"
        val data = hashMapOf<String,Any?>("id" to id,"userId" to uid,"name" to name,"mobile" to mobile,"area" to area,"category" to category,"detail" to detail,"latitude" to lat,"longitude" to lng,"status" to "Submitted","createdAt" to System.currentTimeMillis())
        db.collection("complaints").document(id).set(data).addOnSuccessListener { done("شکایت کامیابی سے جمع ہو گئی۔\nTracking ID: $id") }.addOnFailureListener { done("شکایت جمع نہیں ہو سکی: ${it.localizedMessage}") }
    }

    @Composable private fun MyComplaints() {
        var list by remember { mutableStateOf(emptyList<Complaint>()) }
        var loading by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) { ensureAuth { loadComplaints { list = it; loading = false } } }
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("میری شکایات", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (loading) CircularProgressIndicator()
            else if (list.isEmpty()) Text("ابھی کوئی شکایت موجود نہیں۔")
            else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(list) { c -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) { Text(c.id, fontWeight = FontWeight.Bold); Text(c.category); Text(c.detail); Text("Status: ${c.status}", color = Green) } } } }
        }
    }

    private fun ensureAuth(done:()->Unit) {
        val after = {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                auth.currentUser?.uid?.let { db.collection("users").document(it).set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge()) }
            }
            done()
        }
        if (auth.currentUser != null) after() else auth.signInAnonymously().addOnCompleteListener { after() }
    }
    private fun loadComplaints(done:(List<Complaint>)->Unit) {
        val uid = auth.currentUser?.uid ?: return done(emptyList())
        db.collection("complaints").whereEqualTo("userId",uid).orderBy("createdAt",Query.Direction.DESCENDING).get().addOnSuccessListener { snap ->
            done(snap.documents.map { d -> Complaint(d.getString("id") ?: d.id, d.getString("category") ?: "", d.getString("detail") ?: "", d.getString("area") ?: "", d.getString("status") ?: "Submitted", d.getLong("createdAt") ?: 0L) })
        }.addOnFailureListener { done(emptyList()) }
    }

    @Composable private fun Contact() { Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("رابطہ", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("Public Health Engineering Division Kohistan Upper"); Text("شکایات اور فوری ہنگامی امداد"); Button(onClick = { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$HELPLINE"))) }, modifier = Modifier.fillMaxWidth()) { Text("0343 9398790 پر کال کریں") }; Text("صاف پانی، صحت مند زندگی، خوشحال کوہستان") } }
}
