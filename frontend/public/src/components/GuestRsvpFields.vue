<template>
  <div class="flex flex-col gap-3">
    <fieldset class="flex flex-col gap-2">
      <legend class="rsvp-legend mb-2 font-semibold text-text">
        {{ t('rsvp.question') }}<span class="rsvp-required" :aria-label="t('rsvp.required')">*</span>
      </legend>

      <label
        v-for="choice in choices"
        :key="choice"
        class="rsvp-option flex cursor-pointer items-center gap-3 rounded-xl border border-secondary text-text"
        :class="{ 'border-primary bg-primary/5 font-semibold': modelValue.attendance === choice }"
      >
        <input
          class="h-4 w-4 accent-primary"
          type="radio"
          :name="`${namePrefix}attendance`"
          :value="choice"
          :checked="modelValue.attendance === choice"
          @change="select(choice)"
        />
        {{ t(choice === 'ATTENDING' ? 'rsvp.attending' : 'rsvp.declined') }}
      </label>
    </fieldset>

    <fieldset v-if="modelValue.attendance === 'ATTENDING'" class="flex flex-col gap-2">
      <legend class="rsvp-legend mb-2 font-semibold text-text">
        {{ t('rsvp.meal.question') }}<span class="rsvp-required" :aria-label="t('rsvp.required')">*</span>
      </legend>

      <label
        v-for="meal in meals"
        :key="meal"
        class="rsvp-option flex cursor-pointer items-center gap-3 rounded-xl border border-secondary text-text"
        :class="{ 'border-primary bg-primary/5 font-semibold': modelValue.meal === meal }"
      >
        <input
          class="h-4 w-4 accent-primary"
          type="radio"
          :name="`${namePrefix}meal`"
          :value="meal"
          :checked="modelValue.meal === meal"
          @change="selectMeal(meal)"
        />
        {{ t(mealLabels[meal]) }}
      </label>
    </fieldset>

    <fieldset v-if="modelValue.attendance === 'ATTENDING'" class="flex flex-col gap-2">
      <legend class="rsvp-legend mb-2 font-semibold text-text">
        {{ t('rsvp.song.label') }}<span class="rsvp-optional-hint">({{ t('rsvp.optional') }})</span>
      </legend>

      <div
        v-if="modelValue.song"
        class="song-control flex flex-wrap items-center gap-2 rounded-lg border border-primary bg-primary/5 text-text"
      >
        <div class="flex min-w-0 flex-1 flex-col">
          <span class="truncate font-semibold">{{ modelValue.song.title }}</span>
          <span class="truncate text-text/60">{{ modelValue.song.artist }}</span>
        </div>
        <button
          v-if="modelValue.song.preview"
          type="button"
          class="song-icon flex shrink-0 cursor-pointer items-center justify-center rounded-full border border-primary/40 text-primary transition hover:bg-primary/5"
          :aria-label="t('rsvp.song.preview')"
          :aria-pressed="playingSongId === modelValue.song.deezerId"
          @click="togglePreview(modelValue.song)"
        >
          <span aria-hidden="true">{{ playingSongId === modelValue.song.deezerId ? '⏸' : '▶' }}</span>
        </button>
        <button
          type="button"
          class="song-icon flex shrink-0 cursor-pointer items-center justify-center rounded-full border border-red-600/40 text-red-600 transition hover:bg-red-50"
          :aria-label="t('rsvp.song.clear')"
          :title="t('rsvp.song.clear')"
          @click="clearSong"
        >
          <span aria-hidden="true">✕</span>
        </button>
      </div>

      <template v-else>
        <input
          v-model="songQuery"
          type="search"
          name="song"
          autocomplete="off"
          class="song-control w-full rounded-xl border border-secondary text-text placeholder:text-text/40 focus:border-primary focus:outline-none"
          :placeholder="t('rsvp.song.placeholder')"
          :aria-label="t('rsvp.song.label')"
        />

        <p v-if="songState === 'searching'" class="song-status text-text/70" role="status">
          {{ t('rsvp.song.searching') }}
        </p>
        <p v-else-if="songState === 'error'" class="song-status text-red-600" role="alert">
          {{ t('rsvp.song.error') }}
        </p>
        <p v-else-if="songState === 'no-results'" class="song-status text-text/70" role="status">
          {{ t('rsvp.song.noResults') }}
        </p>

        <ul v-if="songSuggestions.length > 0" class="flex flex-col gap-1">
          <li
            v-for="suggestion in songSuggestions"
            :key="suggestion.deezerId"
            class="song-control flex items-center gap-2 rounded-lg border border-secondary transition-colors hover:border-primary hover:bg-primary/5"
          >
            <button
              type="button"
              class="song-select flex min-w-0 flex-1 cursor-pointer items-center gap-2 text-left text-text"
              @click="selectSong(suggestion)"
            >
              <span
                aria-hidden="true"
                class="song-add flex shrink-0 items-center justify-center rounded-full bg-accent-gradient font-semibold text-white"
              >＋</span>
              <span class="min-w-0 flex-1 truncate font-medium">{{ suggestion.title }}</span>
              <span class="min-w-0 max-w-[40%] truncate text-text/60">{{ suggestion.artist }}</span>
            </button>
            <button
              v-if="suggestion.preview"
              type="button"
              class="song-icon flex shrink-0 cursor-pointer items-center justify-center rounded-full border border-primary/40 text-primary transition hover:bg-primary/5"
              :aria-label="t('rsvp.song.preview')"
              :aria-pressed="playingSongId === suggestion.deezerId"
              @click="togglePreview(suggestion)"
            >
              <span aria-hidden="true">{{ playingSongId === suggestion.deezerId ? '⏸' : '▶' }}</span>
            </button>
          </li>
        </ul>
      </template>
    </fieldset>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue';
import { searchSongs, type Meal, type RsvpAttendance, type Song } from '../services/guestAccessSecuredApi';
import type { RsvpDraft } from '../services/rsvpDraft';
import { useGuestAccessI18n } from '../i18n/guestAccessI18n';
import type { TranslationKey } from '../i18n/messages/types';

type SongState = 'idle' | 'searching' | 'no-results' | 'error';

const SONG_SEARCH_DEBOUNCE_MS = 300;
const SONG_MIN_QUERY_LENGTH = 2;
// Deezer returns results by relevance, so the first few are the closest matches.
const SONG_MAX_SUGGESTIONS = 3;

const choices: RsvpAttendance[] = ['ATTENDING', 'DECLINED'];
const meals: Meal[] = ['MEAT', 'FISH', 'VEGGIE'];
const mealLabels: Record<Meal, TranslationKey> = {
  MEAT: 'rsvp.meal.meat',
  FISH: 'rsvp.meal.fish',
  VEGGIE: 'rsvp.meal.veggie',
};

const props = withDefaults(defineProps<{ modelValue: RsvpDraft; namePrefix?: string }>(), { namePrefix: '' });
const emit = defineEmits<{ 'update:modelValue': [draft: RsvpDraft] }>();

const { t } = useGuestAccessI18n();

const songQuery = ref('');
const songSuggestions = ref<Song[]>([]);
const songState = ref<SongState>('idle');
let songSearchTimer: ReturnType<typeof setTimeout> | null = null;
// Guards against a slow earlier search overwriting a newer one's results.
let songSearchSeq = 0;

// A single reusable audio element plays at most one 30s preview at a time.
const playingSongId = ref<number | null>(null);
let previewAudio: HTMLAudioElement | null = null;

// Cancel a pending debounced call and invalidate any in-flight request: bumping the
// sequence makes a slow response fail its `seq === songSearchSeq` guard, so it can
// never overwrite state the caller is about to reset.
const cancelSongSearch = (): void => {
  if (songSearchTimer !== null) {
    clearTimeout(songSearchTimer);
    songSearchTimer = null;
  }
  songSearchSeq++;
};

const stopPreview = (): void => {
  previewAudio?.pause();
  playingSongId.value = null;
};

const resetSongSearch = (): void => {
  cancelSongSearch();
  stopPreview();
  songQuery.value = '';
  songSuggestions.value = [];
  songState.value = 'idle';
};

const togglePreview = (song: Song): void => {
  if (!song.preview) {
    return;
  }

  if (playingSongId.value === song.deezerId) {
    stopPreview();
    return;
  }

  if (previewAudio === null) {
    previewAudio = new Audio();
    previewAudio.addEventListener('ended', () => {
      playingSongId.value = null;
    });
  }

  previewAudio.src = song.preview;
  playingSongId.value = song.deezerId;

  try {
    const played = previewAudio.play();
    if (played && typeof played.catch === 'function') {
      played.catch(() => {
        playingSongId.value = null;
      });
    }
  } catch {
    playingSongId.value = null;
  }
};

const update = (changes: Partial<RsvpDraft>): void => {
  emit('update:modelValue', { ...props.modelValue, ...changes });
};

const select = (attendance: RsvpAttendance): void => {
  if (attendance !== 'ATTENDING') {
    resetSongSearch();
  }
  update({ attendance });
};

const selectMeal = (meal: Meal): void => update({ meal });

const selectSong = (song: Song): void => {
  resetSongSearch();
  update({ song });
};

const clearSong = (): void => {
  resetSongSearch();
  update({ song: null });
};

const runSongSearch = async (query: string): Promise<void> => {
  const seq = ++songSearchSeq;

  try {
    const results = await searchSongs(query);
    if (seq !== songSearchSeq) {
      return;
    }
    songSuggestions.value = results.slice(0, SONG_MAX_SUGGESTIONS);
    songState.value = results.length === 0 ? 'no-results' : 'idle';
  } catch {
    if (seq !== songSearchSeq) {
      return;
    }
    songSuggestions.value = [];
    songState.value = 'error';
  }
};

watch(songQuery, (query) => {
  cancelSongSearch();

  const trimmed = query.trim();
  if (trimmed.length < SONG_MIN_QUERY_LENGTH) {
    songSuggestions.value = [];
    songState.value = 'idle';
    return;
  }

  songState.value = 'searching';
  songSearchTimer = setTimeout(() => {
    songSearchTimer = null;
    void runSongSearch(trimmed);
  }, SONG_SEARCH_DEBOUNCE_MS);
});

onBeforeUnmount(() => {
  cancelSongSearch();
  previewAudio?.pause();
});
</script>

<style scoped>
/* Fieldsets have an implicit `min-inline-size: min-content`, so a long song
   suggestion would otherwise widen the fieldset (and its full-width search
   input) past the card. Pin them so the input stays a fixed size. */
fieldset {
  min-width: 0;
}

.rsvp-legend {
  margin-bottom: 0.35rem;
  font-size: var(--rsvp-legend-size);
}

.rsvp-required {
  margin-left: 0.15rem;
  color: #a3352b;
  position: relative;
  top: 0.18em;
}

.rsvp-optional-hint {
  margin-left: 0.35rem;
  font-size: var(--rsvp-hint-size);
  font-weight: 400;
  color: rgba(55, 71, 79, 0.55);
}

.rsvp-option {
  font-size: var(--rsvp-text-size);
  padding: var(--rsvp-pad-y) var(--rsvp-pad-x);
}

.song-control {
  font-size: var(--rsvp-text-size);
  padding: var(--rsvp-pad-y) var(--rsvp-pad-x);
}

.song-status {
  font-size: var(--rsvp-status-size);
}

.song-icon {
  width: var(--rsvp-icon-size);
  height: var(--rsvp-icon-size);
  font-size: var(--rsvp-text-size);
}

.song-add {
  width: var(--rsvp-add-size);
  height: var(--rsvp-add-size);
  font-size: var(--rsvp-hint-size);
  line-height: 1;
}
</style>





