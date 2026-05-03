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
