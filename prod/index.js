const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.enviarNotificacaoMensalidadeAluno = functions.firestore
    .document("Alunos/{alunoId}/Mensalidades/{mensalidadeId}")
    .onCreate(async (snapshot, context) => {
      const alunoId = context.params.alunoId;
      const alunoRef = admin.firestore().collection("Alunos").doc(alunoId);

      // Busque o token FCM do aluno
      const alunoDoc = await alunoRef.get();
      const alunoData = alunoDoc.data();

      if (alunoData && alunoData.fcmToken) {
        const payload = {
          notification: {
            title: "Nova Mensalidade",
            body: "Você tem uma nova mensalidade , disponivel para pagamento.",
            // Aqui você pode adicionar outras informações como ícone, som, etc.
          },
          token: alunoData.fcmToken,
        };

        // Envie a notificação ao aluno
        try {
          const response = await admin.messaging().send(payload);
          console.log("Notificação enviada com sucesso:", response);
        } catch (error) {
          console.error("Erro ao enviar notificação:", error);
        }
      } else {
        console.log("Token FCM não encontrado para o aluno:", alunoId);
      }
    });
