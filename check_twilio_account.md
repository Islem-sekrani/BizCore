# Vérification Compte Twilio - Checklist

## ✅ Ce qui fonctionne déjà
- [x] Credentials Twilio configurés
- [x] API Twilio répond (HTTP 201)
- [x] Code d'envoi SMS fonctionne
- [x] Format du numéro correct (+21658410216)

## ⚠️ À vérifier

### 1. Numéro vérifié (CRITIQUE pour compte Trial)
- [ ] Aller sur : https://console.twilio.com/us1/develop/phone-numbers/manage/verified
- [ ] Vérifier si +21658410216 est dans la liste
- [ ] Si NON → Cliquer "Verify a new number" et suivre les étapes

### 2. Logs Twilio
- [ ] Aller sur : https://console.twilio.com/us1/monitor/logs/sms
- [ ] Chercher le SMS envoyé aujourd'hui vers +21658410216
- [ ] Vérifier le statut :
  - ✅ "delivered" = SMS reçu
  - ⏳ "sent" = En cours de livraison
  - ❌ "failed" = Échec (voir le code d'erreur)
  - ⚠️ "undelivered" = Non livré (problème opérateur)

### 3. Codes d'erreur courants

Si le SMS a échoué dans les logs Twilio :

| Code | Signification | Solution |
|------|---------------|----------|
| 21211 | Numéro invalide | Vérifier le format E.164 |
| 21608 | Numéro non vérifié (Trial) | Vérifier le numéro sur Twilio |
| 21610 | Message bloqué | Contenu du SMS bloqué par filtre |
| 30003 | Numéro inaccessible | Téléphone éteint ou hors réseau |
| 30005 | Numéro inconnu | Numéro n'existe pas |
| 30006 | Opérateur a rejeté | Problème avec l'opérateur mobile |

### 4. Test avec un autre numéro

Si vous avez accès à un autre numéro de téléphone :
1. Vérifiez ce numéro sur Twilio
2. Modifiez `TestSMS.java` avec ce numéro
3. Relancez le test

## 🎯 Actions Immédiates

### Action 1 : Vérifier votre numéro (5 minutes)
```
1. Ouvrir : https://console.twilio.com/us1/develop/phone-numbers/manage/verified
2. Cliquer "Verify a new number"
3. Entrer : +21658410216
4. Recevoir le code par SMS
5. Entrer le code
```

### Action 2 : Vérifier les logs (2 minutes)
```
1. Ouvrir : https://console.twilio.com/us1/monitor/logs/sms
2. Chercher le SMS le plus récent
3. Noter le statut et le code d'erreur (si échec)
```

### Action 3 : Upgrade vers Production (optionnel)
Si vous voulez envoyer des SMS à n'importe quel numéro :
```
1. Aller sur : https://console.twilio.com/billing
2. Ajouter une carte de crédit
3. Ajouter du crédit (minimum 20€)
4. Le compte passera automatiquement en mode Production
```

## 📱 Format du Message Actuel

Avec un compte Trial, le SMS reçu ressemblera à :
```
Sent from your Twilio trial account - BizCore - Confirmation de réservation

Bonjour user@example.com,

Votre session de coaching est confirmée :
• Coach : Dupont Lucas - LEADERSHIP
• Contact : +21655987654
• Date : Mercredi 05/03/2026

Nous vous remercions de votre confiance.
```

Avec un compte Production, le préfixe "Sent from your Twilio trial account" disparaît.

## 🆘 Besoin d'Aide ?

Si après avoir vérifié votre numéro, vous ne recevez toujours pas le SMS :
1. Partagez le statut du SMS dans les logs Twilio
2. Partagez le code d'erreur (si présent)
3. Confirmez que votre téléphone peut recevoir des SMS
