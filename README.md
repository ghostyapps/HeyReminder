# **HeyReminder — Simple Daily Reminder App**

HeyReminder is a clean and minimal Android app designed to help users set daily repeating reminders with ease.  
The focus is simplicity, reliability, and a distraction-free user experience.

<p align="center">
  <img src="screenshots/screenshot_1.png" width="600" alt="Screenshots">
</p>

---

## 📱 Features

### ✔ Create Daily Reminders
- Add reminders that repeat on selected days of the week  
- Choose a custom time for each reminder  
- Edit or delete existing reminders easily  

### ✔ Beautiful, Minimal UI
- Clean header with app branding  
- Modern input fields with clear borders  
- Red-accented day selection chips  
- Consistent design in both light and dark themes  

### ✔ Themed Color System
Fully theme-aware using:
- `colors.xml`
- `colors-night.xml`

Includes custom colors such as:
- `header_background`
- `light_accent`
- `text_color`
- `white_background`

The UI automatically adapts to system light/dark mode.

### ✔ Local Notifications
- Each reminder triggers a daily notification at its scheduled time  
- Uses `AlarmManager` for reliable background scheduling  

---

## 🧱 Architecture

The project follows a simple, maintainable structure: