const API = "http://localhost:5000/api";
const IMG = "http://localhost:5000/uploads/";

async function loadDashboard() {
  const res = await fetch(`${API}/complaints`);
  const data = await res.json();

  totalComplaints.innerText = data.length;
  pendingCount.innerText = data.filter(c=>c.status==="Pending").length;
  progressCount.innerText = data.filter(c=>c.status==="In Progress").length;
  resolvedCount.innerText = data.filter(c=>c.status==="Resolved").length;

  pendingList.innerHTML = progressList.innerHTML = resolvedList.innerHTML = "";

  data.forEach(c => {
    const div = document.createElement("div");
    div.className = "complaint-card";

    div.innerHTML = `
      <h4>${c.issueType} | Ward ${c.ward}</h4>
      <p>${c.description}</p>
      <p>${c.addressText}</p>
      <img src="${IMG + c.beforePhoto}">

      ${
        c.status === "Resolved"
        ? `<p>⭐ Rating: ${c.avgRating ?? "No ratings"} (${c.ratingCount})</p>
           ${c.afterPhoto ? `<img src="${IMG + c.afterPhoto}">` : ""}`
        : `
          <select id="s-${c._id}">
            <option ${c.status==="Pending"?"selected":""}>Pending</option>
            <option ${c.status==="In Progress"?"selected":""}>In Progress</option>
            <option>Resolved</option>
          </select>
          <input type="file" id="p-${c._id}">
          <button onclick="updateStatus('${c._id}')">Update</button>
          <button class="delete-btn" onclick="deleteComplaint('${c._id}')">Delete</button>
        `
      }
    `;

    if (c.status === "Pending") pendingList.appendChild(div);
    else if (c.status === "In Progress") progressList.appendChild(div);
    else resolvedList.appendChild(div);
  });
}

async function updateStatus(id) {
  const fd = new FormData();
  fd.append("status", document.getElementById(`s-${id}`).value);
  const f = document.getElementById(`p-${id}`).files[0];
  if (f) fd.append("afterPhoto", f);

  await fetch(`${API}/complaints/${id}`, { method: "PUT", body: fd });
  loadDashboard();
}

async function deleteComplaint(id) {
  if (!confirm("Delete complaint?")) return;
  await fetch(`${API}/complaints/${id}`, { method: "DELETE" });
  loadDashboard();
}

async function loadWardScore() {
  const ward = wardInput.value;
  const res = await fetch(`${API}/ward-score/${ward}`);
  const d = await res.json();
  avgRating.innerText = d.averageRating;
  ratingCount.innerText = d.ratingCount;
}

loadDashboard();
