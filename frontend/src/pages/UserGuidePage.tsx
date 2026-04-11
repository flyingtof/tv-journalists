import React from 'react';

export const UserGuidePage: React.FC = () => {
  return (
    <div style={{ padding: '24px' }}>
      <h1 style={{ fontSize: '24px', fontWeight: 'bold', marginBottom: '24px' }}>Guide Utilisateur - TV Journalists</h1>
      
      <p style={{ marginBottom: '16px' }}>Bienvenue dans l'application de gestion des relations presse et influenceurs de Terre Vivante. Ce guide vous aidera à prendre en main les fonctionnalités principales.</p>

      <section style={{ marginBottom: '32px' }}>
        <h2 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '12px' }}>1. Gestion des Profils</h2>
        <p>Vous pouvez créer et modifier les fiches des journalistes et influenceurs.</p>
        <ul style={{ listStylePosition: 'inside', paddingLeft: '20px' }}>
          <li style={{ marginBottom: '8px' }}><strong>Informations Globales</strong> : Nom, prénom, email et téléphone principal.</li>
          <li style={{ marginBottom: '8px' }}><strong>Activités</strong> : Un profil peut être lié à plusieurs médias (ex: Radio, TV, Presse Web). Pour chaque média, vous pouvez préciser le rôle et les thématiques environnementales suivies.</li>
        </ul>
      </section>

      <section style={{ marginBottom: '32px' }}>
        <h2 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '12px' }}>2. Recherche Multi-Critères</h2>
        <p>L'outil de recherche vous permet de cibler précisément vos contacts pour vos campagnes.</p>
        <ul style={{ listStylePosition: 'inside', paddingLeft: '20px' }}>
          <li style={{ marginBottom: '8px' }}><strong>Par Nom</strong> : Recherche sur le nom ou le prénom.</li>
          <li style={{ marginBottom: '8px' }}><strong>Par Média</strong> : Recherche par le nom de l'organisation.</li>
          <li style={{ marginBottom: '8px' }}><strong>Par Thématiques</strong> : Filtrage par thèmes environnementaux (ex: Biodiversité, Climat).</li>
        </ul>
      </section>

      <section style={{ marginBottom: '32px' }}>
        <h2 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '12px' }}>3. Historique des Interactions</h2>
        <p>Il est essentiel de garder une trace des échanges avec vos contacts.</p>
        <ul style={{ listStylePosition: 'inside', paddingLeft: '20px' }}>
          <li style={{ marginBottom: '8px' }}><strong>Saisie de Notes</strong> : Enregistrez la date et le contenu de vos échanges.</li>
          <li style={{ marginBottom: '8px' }}><strong>Contexte Professionnel</strong> : Vous pouvez lier une interaction à une activité spécifique (ex: "Appel suite au passage à la Radio X").</li>
        </ul>
      </section>

      <section style={{ marginBottom: '32px' }}>
        <h2 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '12px' }}>4. Thématiques Environnementales</h2>
        <p>La liste des thématiques est prédéfinie pour assurer la cohérence de la base de données. Ces thèmes servent de tags pour qualifier les expertises des journalistes.</p>
      </section>
    </div>
  );
};
