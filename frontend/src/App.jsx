import React, {useEffect, useMemo, useRef, useState} from 'react'
import L from 'leaflet'
import {api} from './api'

const statusLabel = s => s.replaceAll('_',' ')
const formatDate = v => v ? new Date(v).toLocaleString() : '—'

function FloatingOrbs(){
  return <div className="ambient" aria-hidden="true">
    <span className="orb orb1"/><span className="orb orb2"/><span className="orb orb3"/>
    <span className="gridGlow"/><span className="noise"/>
  </div>
}

function Brand({admin=false}){
  return <div className="brand"><div className="brandMark">{admin ? '⌘' : '✦'}</div>
    <div><strong>SMART COMPLAINT</strong><span>{admin ? 'Operations Console' : 'Citizen Service Portal'}</span></div>
  </div>
}

function Topbar({admin,onSwitch}){
  return <header className={`topbar ${admin?'glassbar':''}`}>
    <Brand admin={admin}/>
    <button className="switchBtn" onClick={onSwitch}>{admin ? '← USER PORTAL' : 'ADMIN CONSOLE →'}</button>
  </header>
}

function Login({mode,onSuccess,onSwitch}){
  const [value,setValue]=useState('')
  const [error,setError]=useState('')
  const admin=mode==='admin'
  const submit=async e=>{
    e.preventDefault();setError('')
    try{admin ? await api.adminLogin(value) : await api.userLogin(value); onSuccess()}
    catch(err){setError(err.message)}
  }
  return <div className={admin?'adminShell shell':'userShell shell'}>
    <FloatingOrbs/><Topbar admin={admin} onSwitch={onSwitch}/>
    <main className="loginWrap">
      <section className="heroCopy">
        <div className="eyebrow">{admin?'LIVE OPERATIONS • SECURE ACCESS':'CITY SERVICES • DIGITAL FIRST'}</div>
        <h1>{admin ? <>Manage every<br/><em>complaint.</em></> : <>Make the issue<br/><em>impossible to miss.</em></>}</h1>
        <p>{admin ? 'A focused glass control room for triage, assignment, evidence review and resolution.' : 'Report an issue, attach proof, pin the exact location and receive a trackable complaint ID.'}</p>
        <div className="heroChips"><span>01 • REPORT</span><span>02 • LOCATE</span><span>03 • RESOLVE</span></div>
      </section>
      <form className="loginCard glassCard" onSubmit={submit}>
        <div className="cardIcon">{admin?'⌘':'✦'}</div>
        <span className="miniLabel">{admin?'ADMIN ACCESS':'USER ACCESS'}</span>
        <h2>{admin?'Control room login':'Welcome back'}</h2>
        <p>{admin?'Enter the administrator password.':'Enter your 4-digit citizen PIN.'}</p>
        <input autoFocus value={value} onChange={e=>setValue(e.target.value)} type="password"
          inputMode={admin?'text':'numeric'} maxLength={admin?60:4} placeholder={admin?'admin123':'1234'}/>
        <button className="primaryBtn">{admin?'OPEN CONSOLE':'ENTER PORTAL'} <span>↗</span></button>
        {error && <div className="errorBox">⚠ {error}</div>}
        <div className="demoHint">Demo {admin?'password':'PIN'}: <b>{admin?'admin123':'1234'}</b></div>
      </form>
    </main>
  </div>
}

function LocationMap({onChange}){
  const ref=useRef(null), mapRef=useRef(null), markerRef=useRef(null)
  const [address,setAddress]=useState('')
  useEffect(()=>{
    if(!ref.current || mapRef.current) return
    const map=L.map(ref.current,{zoomControl:true}).setView([17.385,78.4867],12)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{
      maxZoom:19,attribution:'© OpenStreetMap contributors'
    }).addTo(map)
    map.on('click', e=>pick(e.latlng.lat,e.latlng.lng))
    mapRef.current=map
    async function pick(lat,lng){
      if(markerRef.current) markerRef.current.remove()
      markerRef.current=L.marker([lat,lng]).addTo(map)
      map.setView([lat,lng],16)
      let addr=''
      try{
        const r=await fetch(`https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}`)
        if(r.ok){const d=await r.json();addr=d.display_name||''}
      }catch{}
      setAddress(addr)
      onChange({latitude:lat,longitude:lng,address:addr})
    }
    return ()=>map.remove()
  },[])

  const locate=()=>{
    navigator.geolocation?.getCurrentPosition(pos=>{
      const {latitude,longitude}=pos.coords
      if(mapRef.current) mapRef.current.setView([latitude,longitude],16)
      if(markerRef.current) markerRef.current.remove()
      markerRef.current=L.marker([latitude,longitude]).addTo(mapRef.current)
      fetch(`https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${latitude}&lon=${longitude}`)
        .then(r=>r.json()).then(d=>{const addr=d.display_name||'';setAddress(addr);onChange({latitude,longitude,address:addr})})
        .catch(()=>onChange({latitude,longitude,address:''}))
    },()=>alert('Location permission was denied or unavailable.'))
  }
  return <div>
    <div ref={ref} className="map"/>
    <div className="mapBottom"><button type="button" className="softBtn" onClick={locate}>⌖ USE MY LOCATION</button>
      <span>{address || 'Click the map to place a pin'}</span></div>
  </div>
}

function UserPortal({onSwitch}){
  const [location,setLocation]=useState({})
  const [proof,setProof]=useState(null)
  const [message,setMessage]=useState('')
  const [trackId,setTrackId]=useState('')
  const [tracked,setTracked]=useState(null)
  const submit=async e=>{
    e.preventDefault();setMessage('')
    const f=new FormData(e.currentTarget)
    if(location.latitude!=null){
      f.set('latitude',location.latitude);f.set('longitude',location.longitude);f.set('address',location.address||'')
    }
    if(proof) f.set('proof',proof)
    try{
      const data=await api.createComplaint(f)
      setMessage(`Submitted successfully. Your complaint ID is ${data.complaintId}.`)
      e.currentTarget.reset();setProof(null);setLocation({})
    }catch(err){setMessage(err.message)}
  }
  const track=async()=>{
    try{setTracked(await api.getComplaint(trackId.trim()))}catch(err){setTracked({error:err.message})}
  }
  return <div className="userShell shell"><FloatingOrbs/><Topbar onSwitch={onSwitch}/>
    <main className="portal">
      <div className="sectionIntro"><div><span className="eyebrow">CITIZEN PORTAL</span><h1>Turn a complaint<br/><em>into action.</em></h1></div><div className="floatingBadge">✦ PROOF + LOCATION<br/><small>One report. One ID.</small></div></div>
      <div className="portalGrid">
        <form className="glassCard complaintCard" onSubmit={submit}>
          <div className="cardHeader"><div><span className="miniLabel">NEW REPORT</span><h2>What happened?</h2></div><span className="step">01</span></div>
          <label>Complaint type<select name="type" required><option>Road / Street</option><option>Water Supply</option><option>Garbage / Sanitation</option><option>Electricity</option><option>Public Safety</option><option>Other</option></select></label>
          <label>Description<textarea name="description" required placeholder="Describe the issue, where it happened, and anything useful for the department."/></label>
          <label>Contact information<input name="contact" required placeholder="Phone or email"/></label>
          <div className="proofBox"><div><span className="miniLabel">EVIDENCE</span><h3>Attach proof photo</h3><p>JPG, PNG or WEBP • max 10 MB</p></div>
            <label className="uploadBtn">+ CHOOSE PHOTO<input type="file" accept="image/*" onChange={e=>setProof(e.target.files?.[0]||null)}/></label>
            {proof && <div className="proofPreview"><img src={URL.createObjectURL(proof)}/><span>{proof.name}</span></div>}
          </div>
          <button className="primaryBtn big">SUBMIT COMPLAINT <span>↗</span></button>
          {message && <div className="successBox">{message}</div>}
        </form>
        <section className="glassCard mapCard"><div className="cardHeader"><div><span className="miniLabel">LOCATION</span><h2>Pin the issue</h2></div><span className="step">02</span></div>
          <LocationMap onChange={setLocation}/>
          <div className="locationMeta"><span>● LOCATION IS OPTIONAL</span><span>OPENSTREETMAP</span></div>
        </section>
      </div>
      <section className="glassCard tracker"><div><span className="miniLabel">TRACKING</span><h2>Where is my complaint?</h2></div>
        <div className="trackRow"><input value={trackId} onChange={e=>setTrackId(e.target.value)} placeholder="Enter CMP-XXXXXXXX"/><button className="primaryBtn" onClick={track}>CHECK STATUS</button></div>
        {tracked && <div className="trackResult">{tracked.error ? <b>⚠ {tracked.error}</b> : <><strong>{tracked.complaintId}</strong><span>{statusLabel(tracked.status)}</span><p>{tracked.type} • Updated {formatDate(tracked.updatedAt)}</p>{tracked.address && <small>⌖ {tracked.address}</small>}</>}</div>}
      </section>
    </main>
  </div>
}

function MiniMap({lat,lng,id}){
  const ref=useRef(null)
  useEffect(()=>{
    if(!ref.current || lat==null || lng==null) return
    const map=L.map(ref.current,{zoomControl:false,attributionControl:false}).setView([lat,lng],15)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map)
    L.marker([lat,lng]).addTo(map)
    return ()=>map.remove()
  },[lat,lng])
  return <div ref={ref} className="miniMap" title={`Location ${id}`}/>
}

function AdminPortal({onSwitch}){
  const [data,setData]=useState([]),[filter,setFilter]=useState('ALL'),[search,setSearch]=useState(''),[selected,setSelected]=useState(null)
  const load=()=>api.allComplaints().then(setData).catch(()=>setData([]))
  useEffect(()=>{load()},[])
  const visible=useMemo(()=>data.filter(c=>(filter==='ALL'||c.status===filter)&&(`${c.complaintId} ${c.type} ${c.description}`.toLowerCase().includes(search.toLowerCase()))),[data,filter,search])
  const counts={total:data.length,pending:data.filter(x=>x.status==='PENDING').length,progress:data.filter(x=>x.status==='IN_PROGRESS').length,resolved:data.filter(x=>x.status==='RESOLVED').length}
  const update=async(id,body)=>{await api.updateComplaint(id,body);load()}
  return <div className="adminShell shell"><FloatingOrbs/><Topbar admin onSwitch={onSwitch}/>
    <main className="adminPortal">
      <div className="adminHero"><div><span className="eyebrow">OPERATIONS • LIVE</span><h1>Complaint<br/><em>control room.</em></h1><p>Review evidence, locate incidents, assign staff and move every case toward resolution.</p></div><div className="pulseCard glassCard"><span className="liveDot"/> SYSTEM ONLINE<div className="pulseLine"/></div></div>
      <div className="statsGrid"><div className="stat glassCard"><span>TOTAL CASES</span><b>{counts.total}</b><small>All complaints</small></div><div className="stat glassCard"><span>PENDING</span><b>{counts.pending}</b><small>Awaiting action</small></div><div className="stat glassCard"><span>IN PROGRESS</span><b>{counts.progress}</b><small>Assigned cases</small></div><div className="stat glassCard"><span>RESOLVED</span><b>{counts.resolved}</b><small>Closed successfully</small></div></div>
      <section className="glassCard adminTableCard">
        <div className="tableToolbar"><div><span className="miniLabel">CASE DATABASE</span><h2>All complaints</h2></div><div className="tools"><input value={search} onChange={e=>setSearch(e.target.value)} placeholder="Search cases..."/><select value={filter} onChange={e=>setFilter(e.target.value)}><option>ALL</option><option>PENDING</option><option>IN_PROGRESS</option><option>RESOLVED</option><option>REJECTED</option></select></div></div>
        <div className="tableWrap"><table><thead><tr><th>CASE</th><th>ISSUE</th><th>STATUS</th><th>ASSIGNEE</th><th>LOCATION</th><th>PROOF</th></tr></thead><tbody>
          {visible.map(c=><tr key={c.complaintId}><td><b>{c.complaintId}</b><small>{formatDate(c.createdAt)}</small></td><td><b>{c.type}</b><p>{c.description}</p><small>{c.contact}</small></td>
            <td><select className={`statusSelect ${c.status}`} value={c.status} onChange={e=>update(c.complaintId,{status:e.target.value})}><option>PENDING</option><option>IN_PROGRESS</option><option>RESOLVED</option><option>REJECTED</option></select></td>
            <td><input className="staffInput" defaultValue={c.assignedStaff||''} placeholder="Assign staff" onBlur={e=>{if(e.target.value!==(c.assignedStaff||''))update(c.complaintId,{assignedStaff:e.target.value})}}/></td>
            <td>{c.latitude!=null&&c.longitude!=null ? <><MiniMap lat={c.latitude} lng={c.longitude} id={c.complaintId}/><small className="address">{c.address||`${c.latitude.toFixed(5)}, ${c.longitude.toFixed(5)}`}</small></> : <span className="muted">No pin</span>}</td>
            <td>{c.proofFileName ? <button className="proofBtn" onClick={()=>setSelected(c)}>VIEW PROOF ↗</button> : <span className="muted">No proof</span>}</td>
          </tr>)}
        </tbody></table>{!visible.length&&<div className="empty">No complaints match your filters.</div>}</div>
      </section>
    </main>
    {selected && <div className="modalBack" onClick={()=>setSelected(null)}><div className="proofModal glassCard" onClick={e=>e.stopPropagation()}><button className="close" onClick={()=>setSelected(null)}>×</button><span className="miniLabel">EVIDENCE • {selected.complaintId}</span><h2>{selected.proofFileName}</h2><img src={api.proofUrl(selected.complaintId)} alt="Complaint proof"/><p>{selected.description}</p></div></div>}
  </div>
}

export default function App(){
  const [screen,setScreen]=useState('login-user')
  const goUser=()=>setScreen('login-user'), goAdmin=()=>setScreen('login-admin')
  if(screen==='user') return <UserPortal onSwitch={goAdmin}/>
  if(screen==='admin') return <AdminPortal onSwitch={goUser}/>
  return <Login mode={screen.endsWith('admin')?'admin':'user'} onSwitch={screen.endsWith('admin')?goUser:goAdmin} onSuccess={()=>setScreen(screen.endsWith('admin')?'admin':'user')}/>
}
