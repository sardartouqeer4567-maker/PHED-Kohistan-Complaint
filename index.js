const {onDocumentUpdated}=require('firebase-functions/v2/firestore');
const admin=require('firebase-admin'); admin.initializeApp();
exports.onComplaintStatusChanged=onDocumentUpdated('complaints/{id}',async event=>{
  const before=event.data.before.data(), after=event.data.after.data();
  if(before.status===after.status) return null;
  const uid=after.userId; if(!uid) return null;
  const user=await admin.firestore().collection('users').doc(uid).get();
  const token=user.get('fcmToken'); if(!token) return null;
  return admin.messaging().send({token,notification:{title:'PHED Kohistan Upper',body:`Complaint ${after.id}: ${after.status}`},data:{complaintId:after.id,status:after.status}});
});
