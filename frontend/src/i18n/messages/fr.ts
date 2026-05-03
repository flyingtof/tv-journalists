import type { Messages } from '../types';

export const frMessages: Messages = {
  greeting: 'Bonjour',
  welcome: 'Bienvenue, {name}!',
  
  // App shell and navigation
  'app.logo': 'TV Journalists',
  'app.nav.search': 'Recherche',
  'app.nav.userGuide': 'Guide Utilisateur',
  'app.nav.users': 'Utilisateurs',
  'app.nav.themes': 'Thèmes',
  'app.nav.loading': 'Chargement...',
  'app.nav.logout': 'Se déconnecter',
  'app.role.admin': 'Administrateur',
  'app.role.themeManager': 'Gestionnaire des thèmes',
  'app.role.user': 'Utilisateur',
  
  // Protected route
  'protectedRoute.loading': 'Chargement de la session...',
  
  // Login page
  'login.username': 'Utilisateur',
  'login.password': 'Mot de passe',
  'login.submit': 'Se connecter',
  'login.invalidCredentials': 'Identifiants invalides. Veuillez réessayer.',
  'login.logoutSuccess': 'Vous avez été déconnecté avec succès.',
  
  // Journalist search page
  'journalistSearch.title': 'Recherche de journalistes',
  'journalistSearch.nameLabel': 'Nom',
  'journalistSearch.mediaLabel': 'Média',
  'journalistSearch.themesLabel': 'Thèmes',
  'journalistSearch.search': 'Rechercher',
  'journalistSearch.loadError': 'Impossible de charger la liste des journalistes. Veuillez réessayer.',
  'journalistSearch.pagination.previous': 'Précédent',
  'journalistSearch.pagination.next': 'Suivant',
  'journalistSearch.pagination.summary': '{start}-{end} sur {total}',
  'journalistSearch.pagination.emptyState': '0-0 sur 0',
  
  // Journalist profile page
  'journalistProfile.notFound': 'Journaliste introuvable',
  'journalistProfile.loadError': 'Impossible de charger la fiche journaliste. Veuillez réessayer.',
  'journalistProfile.loading': 'Chargement...',
  'journalistProfile.contact.title': 'Informations de Contact',
  'journalistProfile.activities.title': 'Activités Média',
  'journalistProfile.activities.empty': 'Aucune activité média enregistrée.',
  'journalistProfile.back': 'Retour',
  
  // Journalist list
  'journalistList.empty': 'Aucun journaliste trouvé.',
  'journalistList.columnName': 'Nom',
  'journalistList.columnEmail': 'Email',
  'journalistList.columnMedia': 'Médias',

  // Interaction log form
  'interactionLog.title': 'Log New Interaction',
  'interactionLog.labelDate': 'Date',
  'interactionLog.labelActivity': 'Related Activity (Optional)',
  'interactionLog.activityNone': 'None',
  'interactionLog.labelDescription': 'Description',
  'interactionLog.submit': 'Log Interaction',

  // Journalist form
  'journalistForm.labelFirstName': 'First Name',
  'journalistForm.labelLastName': 'Last Name',
  'journalistForm.labelEmail': 'Email',
  'journalistForm.labelPhone': 'Phone',
  'journalistForm.submit': 'Save Profile',
};
