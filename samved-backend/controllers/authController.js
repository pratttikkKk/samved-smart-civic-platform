const User = require("../models/User");

// Register
exports.register = async (req, res) => {
  const { name, mobile, ward, password } = req.body;

  if (!name || !mobile || !ward || !password) {
    return res.status(400).json({ message: "All fields required" });
  }

  try {
    const exists = await User.findOne({ mobile });
    if (exists) return res.status(400).json({ message: "User already exists" });

    const user = new User({ name, mobile, ward, password });
    await user.save();

    res.json({ message: "User registered" });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

// Login
exports.login = async (req, res) => {
  const { mobile, password } = req.body;

  if (!mobile || !password) {
    return res.status(400).json({ message: "Mobile and password required" });
  }

  try {
    const user = await User.findOne({ mobile, password });
    if (!user) return res.status(401).json({ message: "Invalid credentials" });

    res.json({ message: "Login success", user });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};
