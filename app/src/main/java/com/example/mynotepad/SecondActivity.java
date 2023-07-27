package com.example.mynotepad;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SecondActivity extends AppCompatActivity implements NotesAdapter.NoteClickListener {
    private DatabaseHelper databaseHelper;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        databaseHelper = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadNotes();

        findViewById(R.id.btnAddNote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNoteDialog(null);
            }
        });
    }

    private void loadNotes() {
        noteList = databaseHelper.getAllNotes();
        notesAdapter = new NotesAdapter(noteList, this);
        notesAdapter.setNoteClickListener(this);
        recyclerView.setAdapter(notesAdapter);
    }

    private void showNoteDialog(final Note note) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_note, null);
        dialogBuilder.setView(dialogView);

        final EditText etTitle = dialogView.findViewById(R.id.etTitle);
        final EditText etContent = dialogView.findViewById(R.id.etContent);

        if (note != null) {
            etTitle.setText(note.getTitle());
            etContent.setText(note.getContent());
        }

        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String title = etTitle.getText().toString().trim();
                String content = etContent.getText().toString().trim();

                if (title.isEmpty() || content.isEmpty()) {
                    Toast.makeText(SecondActivity.this,
                            "Please enter both title and content!", Toast.LENGTH_SHORT).show();
                } else {
                    if (note != null) {
                        note.setTitle(title);
                        note.setContent(content);
                        databaseHelper.updateNote(note);
                        notesAdapter.notifyDataSetChanged();
                    } else {
                        Note newNote = new Note(title, content, "");
                        long id = databaseHelper.insertNote(newNote);
                        newNote.setId((int) id);
                        noteList.add(newNote);
                        notesAdapter.notifyItemInserted(noteList.size() - 1);
                        recyclerView.smoothScrollToPosition(noteList.size() - 1);
                    }

                    dialog.dismiss();
                }
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        if (note != null) {
            dialogBuilder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    databaseHelper.deleteNote(note);
                    noteList.remove(note);
                    notesAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            });
        }

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onNoteClick(int position) {
        Note note = noteList.get(position);
        showNoteDialog(note);
    }
}
