package org.openjfx.sio2E4.service;

import org.openjfx.sio2E4.repository.*;

public class SyncService {
    private final EvaluationRepository evaluationRepository;
    private final MatiereRepository matiereRepository;
    private final NoteRepository noteRepository;
    private final NoteTypeRepository noteTypeRepository;
    private final UserRepository userRepository;

    public SyncService() {
        this.evaluationRepository = new EvaluationRepository();
        this.matiereRepository = new MatiereRepository();
        this.noteRepository = new NoteRepository();
        this.noteTypeRepository = new NoteTypeRepository();
        this.userRepository = new UserRepository();
    }

    public void init() {
        if (NetworkService.isOnline()) {
            evaluationRepository.fetchEvaluationsListFromApi()
                    .thenAccept(evaluations -> {
                        LocalStorageService.replaceEvaluations(evaluations);
                    })
                    .exceptionally(e -> {
                        System.err.println("Impossible de synchroniser les évaluations");
                        e.printStackTrace();
                        return null;
                    });

            matiereRepository.fetchMatieresListFromApi()
                    .thenAccept(matieres -> {
                        LocalStorageService.replaceMatieres(matieres);
                    })
                    .exceptionally(e -> {
                        System.err.println("Impossible de synchroniser les matières");
                        e.printStackTrace();
                        return null;
                    });

            /*noteRepository.fetchNotesListFromApi()
                    .thenAccept(notes -> {
                        LocalStorageService.replaceNotes(notes);
                    })
                    .exceptionally(e -> {
                        System.err.println("Impossible de synchroniser les notes");
                        e.printStackTrace();
                        return null;
                    });*/

            noteTypeRepository.fetchNoteTypesListFromApi()
                    .thenAccept(noteTypes -> {
                        LocalStorageService.replaceNoteTypes(noteTypes);
                    })
                    .exceptionally(e -> {
                        System.err.println("Impossible de synchroniser les noteTypes");
                        e.printStackTrace();
                        return null;
                    });

            userRepository.fetchUsersListFromApi()
                    .thenAccept(users -> {
                        LocalStorageService.replaceUsers(users);
                    })
                    .exceptionally(e -> {
                        System.err.println("Impossible de synchroniser les utilisateurs");
                        e.printStackTrace();
                        return null;
                    });
        }
    }
}
