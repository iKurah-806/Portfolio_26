"use strict";

window.addEventListener("DOMContentLoaded", () => {
  const mailInput = document.getElementById("mailAddress");
  const passInput = document.getElementById("password");
  const keepInput = document.getElementById("keep");
  const loginForm = document.getElementById("loginForm");

  if (!mailInput || !passInput || !keepInput || !loginForm) return;

  // --- 自動入力 & 自動submit ---
  const savedData = localStorage.getItem("loginData");
  if (savedData) {
    try {
      const { mailAddress, password } = JSON.parse(savedData);
      mailInput.value = mailAddress;
      passInput.value = password;
      keepInput.checked = true;

      // 自動submitでログイン
      loginForm.submit();
    } catch (err) {
      console.error("loginData の読み込みに失敗:", err);
    }
  }

  // --- 保存処理 ---
  loginForm.addEventListener("submit", () => {
    const mailAddress = mailInput.value.trim();
    const password = passInput.value.trim();
    const keep = keepInput.checked;

    if (keep && mailAddress && password) {
      localStorage.setItem("loginData", JSON.stringify({ mailAddress, password }));
    } else {
      localStorage.removeItem("loginData");
    }

    sessionStorage.setItem("fromLogin", "true");
  });
});
