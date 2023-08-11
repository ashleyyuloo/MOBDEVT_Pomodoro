// Generated by view binder compiler. Do not edit!
package com.example.pomodoro.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.pomodoro.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ItemTaskBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Button btnDeleteTask;

  @NonNull
  public final CheckBox checkboxCompleted;

  @NonNull
  public final EditText txtTask;

  private ItemTaskBinding(@NonNull LinearLayout rootView, @NonNull Button btnDeleteTask,
      @NonNull CheckBox checkboxCompleted, @NonNull EditText txtTask) {
    this.rootView = rootView;
    this.btnDeleteTask = btnDeleteTask;
    this.checkboxCompleted = checkboxCompleted;
    this.txtTask = txtTask;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemTaskBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemTaskBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_task, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemTaskBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnDeleteTask;
      Button btnDeleteTask = ViewBindings.findChildViewById(rootView, id);
      if (btnDeleteTask == null) {
        break missingId;
      }

      id = R.id.checkboxCompleted;
      CheckBox checkboxCompleted = ViewBindings.findChildViewById(rootView, id);
      if (checkboxCompleted == null) {
        break missingId;
      }

      id = R.id.txtTask;
      EditText txtTask = ViewBindings.findChildViewById(rootView, id);
      if (txtTask == null) {
        break missingId;
      }

      return new ItemTaskBinding((LinearLayout) rootView, btnDeleteTask, checkboxCompleted,
          txtTask);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
