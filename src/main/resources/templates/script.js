document.addEventListener('DOMContentLoaded', () => {
    console.log("script.js cargado correctamente");
  // --- Parallax ---
  const parallaxBg = document.getElementById('parallax-bg');
  const intensity = 30;


  if (parallaxBg) {
    document.addEventListener('mousemove', (e) => {
      const w = window.innerWidth;
      const h = window.innerHeight;
      const centerX = w / 2;
      const centerY = h / 2;

      const offsetX = (e.clientX - centerX) / centerX;
      const offsetY = (e.clientY - centerY) / centerY;

      const moveX = -offsetX * intensity;
      const moveY = -offsetY * intensity;

      parallaxBg.style.transform = `scale(1.1) translate(${moveX}px, ${moveY}px)`;
    });
  }

  // --- Mostrar/ocultar contraseña ---
  const togglePasswordBtns = document.querySelectorAll('.toggle-password');

  togglePasswordBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const input = btn.previousElementSibling;
      if (!input) return;

      const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
      input.setAttribute('type', type);
      btn.classList.toggle('fa-eye');
      btn.classList.toggle('fa-eye-slash');
    });
  });

  // --- Flip Login / Registro ---
  const cardWrapper = document.getElementById('card-wrapper');
  const showRegister = document.getElementById('show-register');
  const showLogin = document.getElementById('show-login');

  if (showRegister && showLogin && cardWrapper) {
    showRegister.addEventListener('click', (e) => {
      e.preventDefault();
      cardWrapper.classList.add('flipped');
    });

    showLogin.addEventListener('click', (e) => {
      e.preventDefault();
      cardWrapper.classList.remove('flipped');
    });
  }

    // --- Registro de usuario ---
    const registerForm = document.getElementById("registerForm");

    if (registerForm) {
        registerForm.addEventListener("submit", async (e) => {
            e.preventDefault();

            const username = document.getElementById("username").value.trim();
            const email = document.getElementById("email").value.trim();
            const password = document.getElementById("password").value.trim();
            const confirmPassword = document.getElementById("confirmPassword").value.trim();

            if (password !== confirmPassword) {
                alert("Las contraseñas no coinciden");
                return;
            }

            const data = {
                username: username,
                email: email,
                password: password,
                estado: "ACTIVO"
            };

            try {
                const response = await fetch("http://localhost:8080/api/users", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(data)
                });

                if (response.ok) {
                    alert("Usuario registrado correctamente");
                    registerForm.reset();
                } else {
                    alert("Error al registrar usuario");
                }
            } catch (error) {
                alert("Error de conexión con el servidor");
                console.error(error);
            }
        });
    }

    // --- Login de usuario ---
    const loginForm = document.getElementById("loginForm");

    if (loginForm) {
        loginForm.addEventListener("submit", async (e) => {
            e.preventDefault();

            // Obtenemos los campos específicos del login
            const email = document.getElementById("loginEmail").value.trim();
            const password = document.getElementById("loginPassword").value.trim();

            if (!email || !password) {
                alert("Por favor ingresa tu correo y contraseña");
                return;
            }

            try {
                const response = await fetch("http://localhost:8080/api/users/login", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ email, password })
                });

                if (response.ok) {
                    const user = await response.json();

                    alert(`✅ Bienvenido, ${user.username || 'usuario'}!`);
                    localStorage.setItem("user", JSON.stringify(user));

                    // Redirige al panel principal
                    window.location.href = "./InicioLogin.html";
                } else if (response.status === 401) {
                    alert("Correo o contraseña incorrectos");
                } else {
                    alert("Error al iniciar sesión");
                }
            } catch (error) {
                console.error("Error en la conexión:", error);
                alert("Error de conexión con el servidor");
            }
        });
    }

    // RESERVAR CITA
    const formReserva = document.getElementById("formReserva");

    if (formReserva) {
        formReserva.addEventListener("submit", async (e) => {
            e.preventDefault();

            const user = JSON.parse(localStorage.getItem("user"));
            if (!user) {
                alert("⚠️ Debes iniciar sesión antes de agendar una cita.");
                window.location.href = "login.html";
                return;
            }

            // --- Datos del vehículo ---
            const marca = document.getElementById("marca").value;
            const modelo = document.getElementById("modelo").value;
            const placa = document.getElementById("placa").value;
            const anio = document.getElementById("anio").value;
            const nota = document.getElementById("nota").value;

            // --- Datos de la reserva ---
            const fecha = document.getElementById("fecha").value;
            const hora = document.getElementById("hora").value;
            const servicio = document.getElementById("servicio").value; // si lo tienes
            const descripcion = document.getElementById("descripcion").value;

            try {
                // 1️⃣ Registrar el vehículo
                const autoResponse = await fetch("http://localhost:8080/api/automoviles", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        marca,
                        modelo,
                        placa,
                        anio,
                        nota
                    })
                });

                if (!autoResponse.ok) {
                    throw new Error("Error al registrar el vehículo");
                }

                const autoData = await autoResponse.json();

                // 2️⃣ Registrar la cita asociada
                const reservaResponse = await fetch("http://localhost:8080/api/reservas", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        fecha,
                        hora,
                        servicio,
                        descripcion,
                        usuarioId: user.id,
                        automovilId: autoData.id
                    })
                });

                if (!reservaResponse.ok) {
                    throw new Error("Error al registrar la cita");
                }

                alert("✅ Cita registrada correctamente!");
                formReserva.reset();

            } catch (error) {
                console.error("Error:", error);
                alert("❌ Ocurrió un error al registrar la cita.");
            }
        });
    }

});
