import {initializeApp} from 'https://www.gstatic.com/firebasejs/12.1.0/firebase-app.js';
import {getAuth,signInWithEmailAndPassword,signOut,onAuthStateChanged} from 'https://www.gstatic.com/firebasejs/12.1.0/firebase-auth.js';
import {getFirestore,collection,getDocs,query,orderBy,doc,updateDoc} from 'https://www.gstatic.com/firebasejs/12.1.0/firebase-firestore.js';
const firebaseConfig={apiKey:'YOUR_API_KEY',authDomain:'YOUR_PROJECT.firebaseapp.com',projectId:'YOUR_PROJECT',storageBucket:'YOUR_PROJECT.firebasestorage.app',messagingSenderId:'YOUR_SENDER_ID',appId:'YOUR_APP_ID'};
const app=initializeApp(firebaseConfig),auth=getAuth(app),db=getFirestore(app);
loginBtn.onclick=()=>signInWithEmailAndPassword(auth,email.value,pass.value).catch(e=>alert(e.message)); logout.onclick=()=>signOut(auth);
onAuthStateChanged(auth,u=>{if(u)load()});
async function load(){const s=await getDocs(query(collection(db,'complaints'),orderBy('createdAt','desc')));list.innerHTML='';s.forEach(x=>{const d=x.data(),el=document.createElement('div');el.innerHTML=`<hr><b>${d.id}</b><p>${d.name||''} — ${d.mobile||''}</p><p>${d.category||''}: ${d.detail||''}</p><p>${d.area||''}</p><select><option>Submitted</option><option>In Review</option><option>Assigned</option><option>In Progress</option><option>Resolved</option><option>Rejected</option></select><button>Update</button>`;const sel=el.querySelector('select');sel.value=d.status||'Submitted';el.querySelector('button').onclick=async()=>{await updateDoc(doc(db,'complaints',x.id),{status:sel.value,updatedAt:Date.now()});load()};list.append(el)})}
