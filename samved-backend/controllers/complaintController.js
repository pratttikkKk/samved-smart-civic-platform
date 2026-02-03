const Complaint = require("../models/Complaint");
const Feedback = require("../models/Feedback");

/* ================= CREATE COMPLAINT ================= */
exports.createComplaint = async (req, res) => {
  try {
    const { userId, issueType, description, ward, addressText } = req.body;
    const latitude = Number(req.body.latitude);
    const longitude = Number(req.body.longitude);

    if (!req.file) return res.status(400).json({ message: "Photo required" });

    const weights = {
      "Garbage": 3,
      "Sewage Overflow": 5,
      "Water Leakage": 4,
      "Road Damage": 2,
      "Street Light": 1,
      "Other": 1
    };

    const issueWeight = weights[issueType] || 1;

    const existing = await Complaint.findOne({
      issueType,
      ward,
      status: "Pending",
      latitude: { $gte: latitude - 0.0003, $lte: latitude + 0.0003 },
      longitude: { $gte: longitude - 0.0003, $lte: longitude + 0.0003 }
    });

    if (!existing) {
      const c = new Complaint({
        userId,
        issueType,
        description,
        ward,
        latitude,
        longitude,
        addressText,
        beforePhoto: req.file.filename,
        reporters: [userId],
        priorityScore: issueWeight
      });
      await c.save();
      return res.json(c);
    }

    if (existing.reporters.includes(userId))
      return res.status(400).json({ message: "Already reported" });

    existing.reporters.push(userId);
    existing.priorityScore = issueWeight * existing.reporters.length;
    await existing.save();

    res.json(existing);

  } catch (e) {
    res.status(500).json({ error: e.message });
  }
};
exports.getUserComplaints = async (req, res) => {
  try {
    const userId = req.params.userId.trim(); // 🔥 FIX HERE

    const complaints = await Complaint.find({
      reporters: userId
    }).sort({ createdAt: -1 });

    res.status(200).json(complaints);
  } catch (error) {
    console.error("GET USER COMPLAINTS ERROR:", error);
    res.status(500).json({ error: error.message });
  }
};


/* ================= GET ALL (ADMIN) ================= */
exports.getAllComplaints = async (req, res) => {
  try {
    const complaints = await Complaint.find().lean();

    const ids = complaints.map(c => c._id);
    const feedbacks = await Feedback.find({ complaintId: { $in: ids } });

    const map = {};
    feedbacks.forEach(f => {
      const id = f.complaintId.toString();
      if (!map[id]) map[id] = [];
      map[id].push(f.rating);
    });

    const final = complaints.map(c => {
      const r = map[c._id.toString()] || [];
      return {
        ...c,
        avgRating: r.length ? (r.reduce((a,b)=>a+b,0)/r.length).toFixed(1) : null,
        ratingCount: r.length
      };
    });

    res.json(final);

  } catch (e) {
    res.status(500).json({ error: e.message });
  }
};

/* ================= UPDATE ================= */
exports.updateComplaint = async (req, res) => {
  const update = { status: req.body.status };
  if (req.file) update.afterPhoto = req.file.filename;

  const updated = await Complaint.findByIdAndUpdate(
    req.params.id,
    update,
    { new: true }
  );
  res.json(updated);
};

/* ================= DELETE ================= */
exports.deleteComplaint = async (req, res) => {
  await Complaint.findByIdAndDelete(req.params.id);
  await Feedback.deleteMany({ complaintId: req.params.id });
  res.json({ message: "Deleted" });
};

/* ================= FEEDBACK ================= */
exports.addFeedback = async (req, res) => {
  const fb = new Feedback(req.body);
  await fb.save();
  res.json(fb);
};

/* ================= WARD ANALYTICS ================= */
exports.getWardScore = async (req, res) => {
  const ward = req.params.ward;

  const complaints = await Complaint.find({ ward });
  const ids = complaints.map(c => c._id);

  const feedbacks = await Feedback.find({ complaintId: { $in: ids } });

  const total = feedbacks.reduce((s,f)=>s+f.rating,0);
  const count = feedbacks.length;

  res.json({
    ward,
    totalComplaints: complaints.length,
    ratingCount: count,
    averageRating: count ? (total/count).toFixed(2) : 0
  });
};
