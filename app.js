const TARGET='http://49.156.20.91:120/?key=0eXn09U4wUSsWkcVZZpBqr0jInEc56s1YTG-MJKu_Yk';
const open=()=>{document.getElementById('statusText').textContent='Membuka sistem…';window.location.href=TARGET};
document.getElementById('openApp').addEventListener('click',open);
document.getElementById('reload').addEventListener('click',()=>location.reload());
if('serviceWorker' in navigator) window.addEventListener('load',()=>navigator.serviceWorker.register('sw.js').catch(()=>{}));