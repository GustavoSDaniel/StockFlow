import { Component, Input } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [MatProgressSpinnerModule],
  template: `
    <div class="spinner-container">
      <mat-spinner [diameter]="diameter" />
      @if (message) { <p class="spinner-message">{{ message }}</p> }
    </div>
  `,
  styles: [`
    .spinner-container { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 64px 16px; }
    .spinner-message { margin-top: 16px; color: #666; }
  `]
})
export class LoadingSpinnerComponent {
  @Input() diameter = 40;
  @Input() message = '';
}
