const express = require("express");                   
const mongoose = require("mongoose");      
const cors = require("cors");

const app = express();
const complaintRoutes = require("./routes/complaintRoutes");
const authRoutes = require("./routes/authRoutes");



// middleware
app.use(cors());
app.use(express.json());
app.use("/api", complaintRoutes);
app.use("/api", authRoutes);
app.use("/uploads", express.static("uploads"));




//mongodb connection
mongoose.connect("mongodb://localhost:27017/samvedDB")
.then(() => {
  console.log("MongoDB Connected");
})
.catch((err) => {
  console.log("MongoDB Error:", err);
});

// test route
app.get("/", (req, res) => {
  res.send("SAMVED Backend Running");
});

// start server
app.listen(5000, () => {
  console.log("Server running on port http://localhost:5000");
});
