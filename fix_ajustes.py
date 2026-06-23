import os

file_path = r"C:\Users\USER\AndroidStudioProjects\REPOSALUD\REPOSALUD v2.0\app\src\main\res\layout\activity_profile.xml"

with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

new_chunk = """            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:orientation="vertical">

                <!-- Settings Card -->
                <androidx.cardview.widget.CardView
                    android:id="@+id/cvAjustes"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:clickable="true"
                    android:focusable="true"
                    android:background="?attr/selectableItemBackground"
                    app:cardCornerRadius="16dp"
                    app:cardElevation="2dp">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:gravity="center_vertical"
                        android:padding="16dp">

                        <FrameLayout
                            android:layout_width="48dp"
                            android:layout_height="48dp"
                            android:background="@drawable/bg_category_icon">
                            <ImageView
                                android:layout_width="24dp"
                                android:layout_height="24dp"
                                android:layout_gravity="center"
                                android:src="@drawable/ic_person"
                                app:tint="@color/primary_green" />
                        </FrameLayout>

                        <LinearLayout
                            android:layout_width="0dp"
                            android:layout_height="wrap_content"
                            android:layout_marginStart="16dp"
                            android:layout_weight="1"
                            android:orientation="vertical">
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="Ajustes"
                                android:textColor="@color/dark_slate"
                                android:textSize="16sp"
                                android:textStyle="bold" />
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="Idioma"
                                android:textColor="@color/light_text"
                                android:textSize="12sp" />
                        </LinearLayout>

                        <ImageView
                            android:layout_width="24dp"
                            android:layout_height="24dp"
                            android:src="@drawable/ic_arrow_forward"
                            app:tint="@color/light_text" />
                    </LinearLayout>
                </androidx.cardview.widget.CardView>
            </LinearLayout>

"""

idx_start = content.find('            <LinearLayout\n                android:layout_width="match_parent"\n                android:layout_height="wrap_content"\n                android:layout_marginTop="16dp"\n                android:orientation="horizontal">')
idx_end = content.find('            <!-- Logout Button -->')
if idx_end == -1:
    idx_end = content.find('<!-- Logout Button -->')
    while content[idx_end-1] in [' ', '\n', '\t', '\r']:
        idx_end -= 1
    idx_end += 1

if idx_start != -1 and idx_end != -1:
    new_content = content[:idx_start] + new_chunk + content[idx_end:]
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(new_content)
    print("Fixed successfully!")
else:
    print(f"Could not find boundaries: {idx_start}, {idx_end}")
