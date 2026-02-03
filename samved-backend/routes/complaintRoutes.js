const express = require("express");
const router = express.Router();
const multer = require("multer");
const {
  createComplaint,
  getAllComplaints,
  updateComplaint,
  deleteComplaint,
  addFeedback,
  getWardScore,
  getUserComplaints
} = require("../controllers/complaintController");

const storage = multer.diskStorage({
  destination: "uploads/",
  filename: (req, file, cb) =>
    cb(null, Date.now() + "-" + file.originalname)
});

const upload = multer({ storage });

router.post("/complaints", upload.single("photo"), createComplaint);
router.get("/complaints", getAllComplaints);
router.put("/complaints/:id", upload.single("afterPhoto"), updateComplaint);
router.delete("/complaints/:id", deleteComplaint);

router.post("/feedback", addFeedback);
router.get("/ward-score/:ward", getWardScore);
router.get("/complaints/user/:userId", (req, res, next) => {
  req.params.userId = decodeURIComponent(req.params.userId).trim();
  next();
}, getUserComplaints);

module.exports = router;
